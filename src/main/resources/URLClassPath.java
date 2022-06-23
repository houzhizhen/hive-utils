/*      */ package sun.misc;
/*      */ 
/*      */ import java.io.Closeable;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.net.HttpURLConnection;
/*      */ import java.net.JarURLConnection;
/*      */ import java.net.MalformedURLException;
/*      */ import java.net.URI;
/*      */ import java.net.URL;
/*      */ import java.net.URLConnection;
/*      */ import java.net.URLStreamHandler;
/*      */ import java.net.URLStreamHandlerFactory;
/*      */ import java.security.AccessControlContext;
/*      */ import java.security.AccessControlException;
/*      */ import java.security.AccessController;
/*      */ import java.security.CodeSigner;
/*      */ import java.security.Permission;
/*      */ import java.security.PrivilegedActionException;
/*      */ import java.security.PrivilegedExceptionAction;
/*      */ import java.security.cert.Certificate;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Collections;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.LinkedList;
/*      */ import java.util.List;
/*      */ import java.util.NoSuchElementException;
/*      */ import java.util.Set;
/*      */ import java.util.Stack;
/*      */ import java.util.StringTokenizer;
/*      */ import java.util.jar.Attributes;
/*      */ import java.util.jar.JarEntry;
/*      */ import java.util.jar.JarFile;
/*      */ import java.util.jar.Manifest;
/*      */ import java.util.zip.ZipEntry;
/*      */ import sun.net.util.URLUtil;
/*      */ import sun.net.www.ParseUtil;
/*      */ import sun.security.action.GetPropertyAction;
/*      */ 
/*      */ public class URLClassPath {
/*      */   static final String USER_AGENT_JAVA_VERSION = "UA-Java-Version";
/*      */   
/*   77 */   static final String JAVA_VERSION = AccessController.<String>doPrivileged(new GetPropertyAction("java.version"));
/*      */   
/*   79 */   private static final boolean DEBUG = (AccessController.doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.debug")) != null);
/*      */   
/*   81 */   private static final boolean DEBUG_LOOKUP_CACHE = (AccessController.doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.debugLookupCache")) != null);
/*      */   
/*      */   private static final boolean DISABLE_JAR_CHECKING;
/*      */   
/*      */   private static final boolean DISABLE_ACC_CHECKING;
/*      */   
/*      */   private static final boolean DISABLE_CP_URL_CHECK;
/*      */   
/*      */   private static final boolean DEBUG_CP_URL_CHECK;
/*      */   
/*      */   static {
/*   83 */     String str = AccessController.<String>doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.disableJarChecking"));
/*   85 */     DISABLE_JAR_CHECKING = (str != null) ? ((str.equals("true") || str.equals(""))) : false;
/*   87 */     str = AccessController.<String>doPrivileged(new GetPropertyAction("jdk.net.URLClassPath.disableRestrictedPermissions"));
/*   89 */     DISABLE_ACC_CHECKING = (str != null) ? ((str.equals("true") || str.equals(""))) : false;
/*   92 */     str = AccessController.<String>doPrivileged(new GetPropertyAction("jdk.net.URLClassPath.disableClassPathURLCheck", "true"));
/*   95 */     DISABLE_CP_URL_CHECK = (str != null) ? ((str.equals("true") || str.isEmpty())) : false;
/*   96 */     DEBUG_CP_URL_CHECK = "debug".equals(str);
/*      */   }
/*      */   
/*  100 */   private ArrayList<URL> path = new ArrayList<>();
/*      */   
/*  103 */   Stack<URL> urls = new Stack<>();
/*      */   
/*  106 */   ArrayList<Loader> loaders = new ArrayList<>();
/*      */   
/*  109 */   HashMap<String, Loader> lmap = new HashMap<>();
/*      */   
/*      */   private URLStreamHandler jarHandler;
/*      */   
/*      */   private boolean closed = false;
/*      */   
/*      */   private final AccessControlContext acc;
/*      */   
/*      */   public URLClassPath(URL[] paramArrayOfURL, URLStreamHandlerFactory paramURLStreamHandlerFactory, AccessControlContext paramAccessControlContext) {
/*  137 */     for (byte b = 0; b < paramArrayOfURL.length; b++)
/*  138 */       this.path.add(paramArrayOfURL[b]); 
/*  140 */     push(paramArrayOfURL);
/*  141 */     if (paramURLStreamHandlerFactory != null)
/*  142 */       this.jarHandler = paramURLStreamHandlerFactory.createURLStreamHandler("jar"); 
/*  144 */     if (DISABLE_ACC_CHECKING) {
/*  145 */       this.acc = null;
/*      */     } else {
/*  147 */       this.acc = paramAccessControlContext;
/*      */     } 
/*      */   }
/*      */   
/*      */   public URLClassPath(URL[] paramArrayOfURL) {
/*  155 */     this(paramArrayOfURL, null, null);
/*      */   }
/*      */   
/*      */   public URLClassPath(URL[] paramArrayOfURL, AccessControlContext paramAccessControlContext) {
/*  159 */     this(paramArrayOfURL, null, paramAccessControlContext);
/*      */   }
/*      */   
/*      */   public synchronized List<IOException> closeLoaders() {
/*  163 */     if (this.closed)
/*  164 */       return Collections.emptyList(); 
/*  166 */     LinkedList<IOException> linkedList = new LinkedList();
/*  167 */     for (Loader loader : this.loaders) {
/*      */       try {
/*  169 */         loader.close();
/*  170 */       } catch (IOException iOException) {
/*  171 */         linkedList.add(iOException);
/*      */       } 
/*      */     } 
/*  174 */     this.closed = true;
/*  175 */     return linkedList;
/*      */   }
/*      */   
/*      */   public synchronized void addURL(URL paramURL) {
/*  186 */     if (this.closed)
/*      */       return; 
/*  188 */     synchronized (this.urls) {
/*  189 */       if (paramURL == null || this.path.contains(paramURL))
/*      */         return; 
/*  192 */       this.urls.add(0, paramURL);
/*  193 */       this.path.add(paramURL);
/*  195 */       if (this.lookupCacheURLs != null)
/*  198 */         disableAllLookupCaches(); 
/*      */     } 
/*      */   }
/*      */   
/*      */   public URL[] getURLs() {
/*  207 */     synchronized (this.urls) {
/*  208 */       return this.path.<URL>toArray(new URL[this.path.size()]);
/*      */     } 
/*      */   }
/*      */   
/*      */   public URL findResource(String paramString, boolean paramBoolean) {
/*  223 */     int[] arrayOfInt = getLookupCache(paramString);
/*      */     Loader loader;
/*  224 */     for (byte b = 0; (loader = getNextLoader(arrayOfInt, b)) != null; b++) {
/*  225 */       URL uRL = loader.findResource(paramString, paramBoolean);
/*  226 */       if (uRL != null)
/*  227 */         return uRL; 
/*      */     } 
/*  230 */     return null;
/*      */   }
/*      */   
/*      */   public Resource getResource(String paramString, boolean paramBoolean) {
/*  242 */     if (DEBUG)
/*  243 */       System.err.println("URLClassPath.getResource(\"" + paramString + "\")"); 
/*  247 */     int[] arrayOfInt = getLookupCache(paramString);
/*      */     Loader loader;
/*  248 */     for (byte b = 0; (loader = getNextLoader(arrayOfInt, b)) != null; b++) {
/*  249 */       Resource resource = loader.getResource(paramString, paramBoolean);
/*  250 */       if (resource != null)
/*  251 */         return resource; 
/*      */     } 
/*  254 */     return null;
/*      */   }
/*      */   
/*      */   public Enumeration<URL> findResources(final String name, final boolean check) {
/*  266 */     return new Enumeration<URL>() {
/*  267 */         private int index = 0;
/*      */         
/*  268 */         private int[] cache = URLClassPath.this.getLookupCache(name);
/*      */         
/*  269 */         private URL url = null;
/*      */         
/*      */         private boolean next() {
/*  272 */           if (this.url != null)
/*  273 */             return true; 
/*      */           URLClassPath.Loader loader;
/*  276 */           while ((loader = URLClassPath.this.getNextLoader(this.cache, this.index++)) != null) {
/*  277 */             this.url = loader.findResource(name, check);
/*  278 */             if (this.url != null)
/*  279 */               return true; 
/*      */           } 
/*  282 */           return false;
/*      */         }
/*      */         
/*      */         public boolean hasMoreElements() {
/*  287 */           return next();
/*      */         }
/*      */         
/*      */         public URL nextElement() {
/*  291 */           if (!next())
/*  292 */             throw new NoSuchElementException(); 
/*  294 */           URL uRL = this.url;
/*  295 */           this.url = null;
/*  296 */           return uRL;
/*      */         }
/*      */       };
/*      */   }
/*      */   
/*      */   public Resource getResource(String paramString) {
/*  302 */     return getResource(paramString, true);
/*      */   }
/*      */   
/*      */   public Enumeration<Resource> getResources(final String name, final boolean check) {
/*  314 */     return new Enumeration<Resource>() {
/*  315 */         private int index = 0;
/*      */         
/*  316 */         private int[] cache = URLClassPath.this.getLookupCache(name);
/*      */         
/*  317 */         private Resource res = null;
/*      */         
/*      */         private boolean next() {
/*  320 */           if (this.res != null)
/*  321 */             return true; 
/*      */           URLClassPath.Loader loader;
/*  324 */           while ((loader = URLClassPath.this.getNextLoader(this.cache, this.index++)) != null) {
/*  325 */             this.res = loader.getResource(name, check);
/*  326 */             if (this.res != null)
/*  327 */               return true; 
/*      */           } 
/*  330 */           return false;
/*      */         }
/*      */         
/*      */         public boolean hasMoreElements() {
/*  335 */           return next();
/*      */         }
/*      */         
/*      */         public Resource nextElement() {
/*  339 */           if (!next())
/*  340 */             throw new NoSuchElementException(); 
/*  342 */           Resource resource = this.res;
/*  343 */           this.res = null;
/*  344 */           return resource;
/*      */         }
/*      */       };
/*      */   }
/*      */   
/*      */   public Enumeration<Resource> getResources(String paramString) {
/*  350 */     return getResources(paramString, true);
/*      */   }
/*      */   
/*  353 */   private static volatile boolean lookupCacheEnabled = "true"
/*  354 */     .equals(VM.getSavedProperty("sun.cds.enableSharedLookupCache"));
/*      */   
/*      */   private URL[] lookupCacheURLs;
/*      */   
/*      */   private ClassLoader lookupCacheLoader;
/*      */   
/*      */   synchronized void initLookupCache(ClassLoader paramClassLoader) {
/*  359 */     if ((this.lookupCacheURLs = getLookupCacheURLs(paramClassLoader)) != null) {
/*  360 */       this.lookupCacheLoader = paramClassLoader;
/*      */     } else {
/*  363 */       disableAllLookupCaches();
/*      */     } 
/*      */   }
/*      */   
/*      */   static void disableAllLookupCaches() {
/*  368 */     lookupCacheEnabled = false;
/*      */   }
/*      */   
/*      */   synchronized boolean knownToNotExist(String paramString) {
/*  378 */     if (this.lookupCacheURLs != null && lookupCacheEnabled)
/*  379 */       return knownToNotExist0(this.lookupCacheLoader, paramString); 
/*  383 */     return false;
/*      */   }
/*      */   
/*      */   private synchronized int[] getLookupCache(String paramString) {
/*  406 */     if (this.lookupCacheURLs == null || !lookupCacheEnabled)
/*  407 */       return null; 
/*  410 */     int[] arrayOfInt = getLookupCacheForClassLoader(this.lookupCacheLoader, paramString);
/*  411 */     if (arrayOfInt != null && arrayOfInt.length > 0) {
/*  412 */       int i = arrayOfInt[arrayOfInt.length - 1];
/*  413 */       if (!ensureLoaderOpened(i)) {
/*  414 */         if (DEBUG_LOOKUP_CACHE)
/*  415 */           System.out.println("Expanded loaders FAILED " + this.loaders
/*  416 */               .size() + " for maxindex=" + i); 
/*  418 */         return null;
/*      */       } 
/*      */     } 
/*  422 */     return arrayOfInt;
/*      */   }
/*      */   
/*      */   private boolean ensureLoaderOpened(int paramInt) {
/*  426 */     if (this.loaders.size() <= paramInt) {
/*  428 */       if (getLoader(paramInt) == null)
/*  429 */         return false; 
/*  431 */       if (!lookupCacheEnabled)
/*  433 */         return false; 
/*  435 */       if (DEBUG_LOOKUP_CACHE)
/*  436 */         System.out.println("Expanded loaders " + this.loaders.size() + " to index=" + paramInt); 
/*      */     } 
/*  440 */     return true;
/*      */   }
/*      */   
/*      */   private synchronized void validateLookupCache(int paramInt, String paramString) {
/*  452 */     if (this.lookupCacheURLs != null && lookupCacheEnabled) {
/*  453 */       if (paramInt < this.lookupCacheURLs.length && paramString
/*  454 */         .equals(
/*  455 */           URLUtil.urlNoFragString(this.lookupCacheURLs[paramInt])))
/*      */         return; 
/*  458 */       if (DEBUG || DEBUG_LOOKUP_CACHE)
/*  459 */         System.out.println("WARNING: resource lookup cache invalidated for lookupCacheLoader at " + paramInt); 
/*  462 */       disableAllLookupCaches();
/*      */     } 
/*      */   }
/*      */   
/*      */   private synchronized Loader getNextLoader(int[] paramArrayOfint, int paramInt) {
/*  479 */     if (this.closed)
/*  480 */       return null; 
/*  482 */     if (paramArrayOfint != null) {
/*  483 */       if (paramInt < paramArrayOfint.length) {
/*  484 */         Loader loader = this.loaders.get(paramArrayOfint[paramInt]);
/*  485 */         if (DEBUG_LOOKUP_CACHE)
/*  486 */           System.out.println("HASCACHE: Loading from : " + paramArrayOfint[paramInt] + " = " + loader
/*  487 */               .getBaseURL()); 
/*  489 */         return loader;
/*      */       } 
/*  491 */       return null;
/*      */     } 
/*  494 */     return getLoader(paramInt);
/*      */   }
/*      */   
/*      */   private synchronized Loader getLoader(int paramInt) {
/*  504 */     if (this.closed)
/*  505 */       return null; 
/*  509 */     while (this.loaders.size() < paramInt + 1) {
/*      */       URL uRL;
/*      */       Loader loader;
/*  512 */       synchronized (this.urls) {
/*  513 */         if (this.urls.empty())
/*  514 */           return null; 
/*  516 */         uRL = this.urls.pop();
/*      */       } 
/*  522 */       String str = URLUtil.urlNoFragString(uRL);
/*  523 */       if (this.lmap.containsKey(str))
/*      */         continue; 
/*      */       try {
/*  529 */         loader = getLoader(uRL);
/*  532 */         URL[] arrayOfURL = loader.getClassPath();
/*  533 */         if (arrayOfURL != null)
/*  534 */           push(arrayOfURL); 
/*  536 */       } catch (IOException iOException) {
/*      */         continue;
/*  539 */       } catch (SecurityException securityException) {
/*  543 */         if (DEBUG)
/*  544 */           System.err.println("Failed to access " + uRL + ", " + securityException); 
/*      */         continue;
/*      */       } 
/*  549 */       validateLookupCache(this.loaders.size(), str);
/*  550 */       this.loaders.add(loader);
/*  551 */       this.lmap.put(str, loader);
/*      */     } 
/*  553 */     if (DEBUG_LOOKUP_CACHE)
/*  554 */       System.out.println("NOCACHE: Loading from : " + paramInt); 
/*  556 */     return this.loaders.get(paramInt);
/*      */   }
/*      */   
/*      */   private Loader getLoader(final URL url) throws IOException {
/*      */     try {
/*  564 */       return AccessController.<Loader>doPrivileged(new PrivilegedExceptionAction<Loader>() {
/*      */             public URLClassPath.Loader run() throws IOException {
/*  567 */               String str = url.getFile();
/*  568 */               if (str != null && str.endsWith("/")) {
/*  569 */                 if ("file".equals(url.getProtocol()))
/*  570 */                   return new URLClassPath.FileLoader(url); 
/*  572 */                 return new URLClassPath.Loader(url);
/*      */               } 
/*  575 */               return new URLClassPath.JarLoader(url, URLClassPath.this.jarHandler, URLClassPath.this.lmap, URLClassPath.this.acc);
/*      */             }
/*      */           }this.acc);
/*  579 */     } catch (PrivilegedActionException privilegedActionException) {
/*  580 */       throw (IOException)privilegedActionException.getException();
/*      */     } 
/*      */   }
/*      */   
/*      */   private void push(URL[] paramArrayOfURL) {
/*  588 */     synchronized (this.urls) {
/*  589 */       for (int i = paramArrayOfURL.length - 1; i >= 0; i--)
/*  590 */         this.urls.push(paramArrayOfURL[i]); 
/*      */     } 
/*      */   }
/*      */   
/*      */   public static URL[] pathToURLs(String paramString) {
/*  602 */     StringTokenizer stringTokenizer = new StringTokenizer(paramString, File.pathSeparator);
/*  603 */     URL[] arrayOfURL = new URL[stringTokenizer.countTokens()];
/*  604 */     byte b = 0;
/*  605 */     while (stringTokenizer.hasMoreTokens()) {
/*  606 */       File file = new File(stringTokenizer.nextToken());
/*      */       try {
/*  608 */         file = new File(file.getCanonicalPath());
/*  609 */       } catch (IOException iOException) {}
/*      */       try {
/*  613 */         arrayOfURL[b++] = ParseUtil.fileToEncodedURL(file);
/*  614 */       } catch (IOException iOException) {}
/*      */     } 
/*  617 */     if (arrayOfURL.length != b) {
/*  618 */       URL[] arrayOfURL1 = new URL[b];
/*  619 */       System.arraycopy(arrayOfURL, 0, arrayOfURL1, 0, b);
/*  620 */       arrayOfURL = arrayOfURL1;
/*      */     } 
/*  622 */     return arrayOfURL;
/*      */   }
/*      */   
/*      */   public URL checkURL(URL paramURL) {
/*      */     try {
/*  632 */       check(paramURL);
/*  633 */     } catch (Exception exception) {
/*  634 */       return null;
/*      */     } 
/*  637 */     return paramURL;
/*      */   }
/*      */   
/*      */   static void check(URL paramURL) throws IOException {
/*  646 */     SecurityManager securityManager = System.getSecurityManager();
/*  647 */     if (securityManager != null) {
/*  648 */       URLConnection uRLConnection = paramURL.openConnection();
/*  649 */       Permission permission = uRLConnection.getPermission();
/*  650 */       if (permission != null)
/*      */         try {
/*  652 */           securityManager.checkPermission(permission);
/*  653 */         } catch (SecurityException securityException) {
/*  656 */           if (permission instanceof java.io.FilePermission && permission
/*  657 */             .getActions().indexOf("read") != -1) {
/*  658 */             securityManager.checkRead(permission.getName());
/*  659 */           } else if (permission instanceof java.net.SocketPermission && permission
/*      */             
/*  661 */             .getActions().indexOf("connect") != -1) {
/*  662 */             URL uRL = paramURL;
/*  663 */             if (uRLConnection instanceof JarURLConnection)
/*  664 */               uRL = ((JarURLConnection)uRLConnection).getJarFileURL(); 
/*  666 */             securityManager.checkConnect(uRL.getHost(), uRL
/*  667 */                 .getPort());
/*      */           } else {
/*  669 */             throw securityException;
/*      */           } 
/*      */         }  
/*      */     } 
/*      */   }
/*      */   
/*      */   private static native URL[] getLookupCacheURLs(ClassLoader paramClassLoader);
/*      */   
/*      */   private static native int[] getLookupCacheForClassLoader(ClassLoader paramClassLoader, String paramString);
/*      */   
/*      */   private static native boolean knownToNotExist0(ClassLoader paramClassLoader, String paramString);
/*      */   
/*      */   private static class Loader implements Closeable {
/*      */     private final URL base;
/*      */     
/*      */     private JarFile jarfile;
/*      */     
/*      */     Loader(URL param1URL) {
/*  688 */       this.base = param1URL;
/*      */     }
/*      */     
/*      */     URL getBaseURL() {
/*  695 */       return this.base;
/*      */     }
/*      */     
/*      */     URL findResource(String param1String, boolean param1Boolean) {
/*      */       URL uRL;
/*      */       try {
/*  701 */         uRL = new URL(this.base, ParseUtil.encodePath(param1String, false));
/*  702 */       } catch (MalformedURLException malformedURLException) {
/*  703 */         throw new IllegalArgumentException("name");
/*      */       } 
/*      */       try {
/*  707 */         if (param1Boolean)
/*  708 */           URLClassPath.check(uRL); 
/*  715 */         URLConnection uRLConnection = uRL.openConnection();
/*  716 */         if (uRLConnection instanceof HttpURLConnection) {
/*  717 */           HttpURLConnection httpURLConnection = (HttpURLConnection)uRLConnection;
/*  718 */           httpURLConnection.setRequestMethod("HEAD");
/*  719 */           if (httpURLConnection.getResponseCode() >= 400)
/*  720 */             return null; 
/*      */         } else {
/*  724 */           uRLConnection.setUseCaches(false);
/*  725 */           InputStream inputStream = uRLConnection.getInputStream();
/*  726 */           inputStream.close();
/*      */         } 
/*  728 */         return uRL;
/*  729 */       } catch (Exception exception) {
/*  730 */         return null;
/*      */       } 
/*      */     }
/*      */     
/*      */     Resource getResource(final String name, boolean param1Boolean) {
/*      */       final URL url;
/*      */       final URLConnection uc;
/*      */       try {
/*  737 */         uRL = new URL(this.base, ParseUtil.encodePath(name, false));
/*  738 */       } catch (MalformedURLException malformedURLException) {
/*  739 */         throw new IllegalArgumentException("name");
/*      */       } 
/*      */       try {
/*  743 */         if (param1Boolean)
/*  744 */           URLClassPath.check(uRL); 
/*  746 */         uRLConnection = uRL.openConnection();
/*  747 */         if (uRLConnection instanceof JarURLConnection) {
/*  752 */           JarURLConnection jarURLConnection = (JarURLConnection)uRLConnection;
/*  753 */           this.jarfile = URLClassPath.JarLoader.checkJar(jarURLConnection.getJarFile());
/*      */         } 
/*  756 */         InputStream inputStream = uRLConnection.getInputStream();
/*  757 */       } catch (Exception exception) {
/*  758 */         return null;
/*      */       } 
/*  760 */       return new Resource() {
/*      */           public String getName() {
/*  761 */             return name;
/*      */           }
/*      */           
/*      */           public URL getURL() {
/*  762 */             return url;
/*      */           }
/*      */           
/*      */           public URL getCodeSourceURL() {
/*  763 */             return URLClassPath.Loader.this.base;
/*      */           }
/*      */           
/*      */           public InputStream getInputStream() throws IOException {
/*  765 */             return uc.getInputStream();
/*      */           }
/*      */           
/*      */           public int getContentLength() throws IOException {
/*  768 */             return uc.getContentLength();
/*      */           }
/*      */         };
/*      */     }
/*      */     
/*      */     Resource getResource(String param1String) {
/*  779 */       return getResource(param1String, true);
/*      */     }
/*      */     
/*      */     public void close() throws IOException {
/*  787 */       if (this.jarfile != null)
/*  788 */         this.jarfile.close(); 
/*      */     }
/*      */     
/*      */     URL[] getClassPath() throws IOException {
/*  796 */       return null;
/*      */     }
/*      */   }
/*      */   
/*      */   static class JarLoader extends Loader {
/*      */     private JarFile jar;
/*      */     
/*      */     private final URL csu;
/*      */     
/*      */     private JarIndex index;
/*      */     
/*      */     private MetaIndex metaIndex;
/*      */     
/*      */     private URLStreamHandler handler;
/*      */     
/*      */     private final HashMap<String, URLClassPath.Loader> lmap;
/*      */     
/*      */     private final AccessControlContext acc;
/*      */     
/*      */     private boolean closed = false;
/*      */     
/*  813 */     private static final JavaUtilZipFileAccess zipAccess = SharedSecrets.getJavaUtilZipFileAccess();
/*      */     
/*      */     JarLoader(URL param1URL, URLStreamHandler param1URLStreamHandler, HashMap<String, URLClassPath.Loader> param1HashMap, AccessControlContext param1AccessControlContext) throws IOException {
/*  824 */       super(new URL("jar", "", -1, param1URL + "!/", param1URLStreamHandler));
/*  825 */       this.csu = param1URL;
/*  826 */       this.handler = param1URLStreamHandler;
/*  827 */       this.lmap = param1HashMap;
/*  828 */       this.acc = param1AccessControlContext;
/*  830 */       if (!isOptimizable(param1URL)) {
/*  831 */         ensureOpen();
/*      */       } else {
/*  833 */         String str = param1URL.getFile();
/*  834 */         if (str != null) {
/*  835 */           str = ParseUtil.decode(str);
/*  836 */           File file = new File(str);
/*  837 */           this.metaIndex = MetaIndex.forJar(file);
/*  844 */           if (this.metaIndex != null && !file.exists())
/*  845 */             this.metaIndex = null; 
/*      */         } 
/*  852 */         if (this.metaIndex == null)
/*  853 */           ensureOpen(); 
/*      */       } 
/*      */     }
/*      */     
/*      */     public void close() throws IOException {
/*  861 */       if (!this.closed) {
/*  862 */         this.closed = true;
/*  864 */         ensureOpen();
/*  865 */         this.jar.close();
/*      */       } 
/*      */     }
/*      */     
/*      */     JarFile getJarFile() {
/*  870 */       return this.jar;
/*      */     }
/*      */     
/*      */     private boolean isOptimizable(URL param1URL) {
/*  874 */       return "file".equals(param1URL.getProtocol());
/*      */     }
/*      */     
/*      */     private void ensureOpen() throws IOException {
/*  878 */       if (this.jar == null)
/*      */         try {
/*  880 */           AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
/*      */                 public Void run() throws IOException {
/*  883 */                   if (URLClassPath.DEBUG) {
/*  884 */                     System.err.println("Opening " + URLClassPath.JarLoader.this.csu);
/*  885 */                     Thread.dumpStack();
/*      */                   } 
/*  888 */                   URLClassPath.JarLoader.this.jar = URLClassPath.JarLoader.this.getJarFile(URLClassPath.JarLoader.this.csu);
/*  889 */                   URLClassPath.JarLoader.this.index = JarIndex.getJarIndex(URLClassPath.JarLoader.this.jar, URLClassPath.JarLoader.this.metaIndex);
/*  890 */                   if (URLClassPath.JarLoader.this.index != null) {
/*  891 */                     String[] arrayOfString = URLClassPath.JarLoader.this.index.getJarFiles();
/*  897 */                     for (byte b = 0; b < arrayOfString.length; b++) {
/*      */                       try {
/*  899 */                         URL uRL = new URL(URLClassPath.JarLoader.this.csu, arrayOfString[b]);
/*  901 */                         String str = URLUtil.urlNoFragString(uRL);
/*  902 */                         if (!URLClassPath.JarLoader.this.lmap.containsKey(str))
/*  903 */                           URLClassPath.JarLoader.this.lmap.put(str, null); 
/*  905 */                       } catch (MalformedURLException malformedURLException) {}
/*      */                     } 
/*      */                   } 
/*  910 */                   return null;
/*      */                 }
/*      */               }this.acc);
/*  913 */         } catch (PrivilegedActionException privilegedActionException) {
/*  914 */           throw (IOException)privilegedActionException.getException();
/*      */         }  
/*      */     }
/*      */     
/*      */     static JarFile checkJar(JarFile param1JarFile) throws IOException {
/*  921 */       if (System.getSecurityManager() != null && !URLClassPath.DISABLE_JAR_CHECKING && 
/*  922 */         !zipAccess.startsWithLocHeader(param1JarFile)) {
/*  923 */         IOException iOException = new IOException("Invalid Jar file");
/*      */         try {
/*  925 */           param1JarFile.close();
/*  926 */         } catch (IOException iOException1) {
/*  927 */           iOException.addSuppressed(iOException1);
/*      */         } 
/*  929 */         throw iOException;
/*      */       } 
/*  932 */       return param1JarFile;
/*      */     }
/*      */     
/*      */     private JarFile getJarFile(URL param1URL) throws IOException {
/*  937 */       if (isOptimizable(param1URL)) {
/*  938 */         FileURLMapper fileURLMapper = new FileURLMapper(param1URL);
/*  939 */         if (!fileURLMapper.exists())
/*  940 */           throw new FileNotFoundException(fileURLMapper.getPath()); 
/*  942 */         return checkJar(new JarFile(fileURLMapper.getPath()));
/*      */       } 
/*  944 */       URLConnection uRLConnection = getBaseURL().openConnection();
/*  945 */       uRLConnection.setRequestProperty("UA-Java-Version", URLClassPath.JAVA_VERSION);
/*  946 */       JarFile jarFile = ((JarURLConnection)uRLConnection).getJarFile();
/*  947 */       return checkJar(jarFile);
/*      */     }
/*      */     
/*      */     JarIndex getIndex() {
/*      */       try {
/*  955 */         ensureOpen();
/*  956 */       } catch (IOException iOException) {
/*  957 */         throw new InternalError(iOException);
/*      */       } 
/*  959 */       return this.index;
/*      */     }
/*      */     
/*      */     Resource checkResource(final String name, boolean param1Boolean, final JarEntry entry) {
/*      */       final URL url;
/*      */       try {
/*  971 */         uRL = new URL(getBaseURL(), ParseUtil.encodePath(name, false));
/*  972 */         if (param1Boolean)
/*  973 */           URLClassPath.check(uRL); 
/*  975 */       } catch (MalformedURLException malformedURLException) {
/*  976 */         return null;
/*  978 */       } catch (IOException iOException) {
/*  979 */         return null;
/*  980 */       } catch (AccessControlException accessControlException) {
/*  981 */         return null;
/*      */       } 
/*  984 */       return new Resource() {
/*      */           public String getName() {
/*  985 */             return name;
/*      */           }
/*      */           
/*      */           public URL getURL() {
/*  986 */             return url;
/*      */           }
/*      */           
/*      */           public URL getCodeSourceURL() {
/*  987 */             return URLClassPath.JarLoader.this.csu;
/*      */           }
/*      */           
/*      */           public InputStream getInputStream() throws IOException {
/*  989 */             return URLClassPath.JarLoader.this.jar.getInputStream(entry);
/*      */           }
/*      */           
/*      */           public int getContentLength() {
/*  991 */             return (int)entry.getSize();
/*      */           }
/*      */           
/*      */           public Manifest getManifest() throws IOException {
/*  993 */             SharedSecrets.javaUtilJarAccess().ensureInitialization(URLClassPath.JarLoader.this.jar);
/*  994 */             return URLClassPath.JarLoader.this.jar.getManifest();
/*      */           }
/*      */           
/*      */           public Certificate[] getCertificates() {
/*  997 */             return entry.getCertificates();
/*      */           }
/*      */           
/*      */           public CodeSigner[] getCodeSigners() {
/*  999 */             return entry.getCodeSigners();
/*      */           }
/*      */         };
/*      */     }
/*      */     
/*      */     boolean validIndex(String param1String) {
/* 1009 */       String str = param1String;
/*      */       int i;
/* 1011 */       if ((i = param1String.lastIndexOf("/")) != -1)
/* 1012 */         str = param1String.substring(0, i); 
/* 1017 */       Enumeration<JarEntry> enumeration = this.jar.entries();
/* 1018 */       while (enumeration.hasMoreElements()) {
/* 1019 */         ZipEntry zipEntry = enumeration.nextElement();
/* 1020 */         String str1 = zipEntry.getName();
/* 1021 */         if ((i = str1.lastIndexOf("/")) != -1)
/* 1022 */           str1 = str1.substring(0, i); 
/* 1023 */         if (str1.equals(str))
/* 1024 */           return true; 
/*      */       } 
/* 1027 */       return false;
/*      */     }
/*      */     
/*      */     URL findResource(String param1String, boolean param1Boolean) {
/* 1034 */       Resource resource = getResource(param1String, param1Boolean);
/* 1035 */       if (resource != null)
/* 1036 */         return resource.getURL(); 
/* 1038 */       return null;
/*      */     }
/*      */     
/*      */     Resource getResource(String param1String, boolean param1Boolean) {
/* 1045 */       if (this.metaIndex != null && 
/* 1046 */         !this.metaIndex.mayContain(param1String))
/* 1047 */         return null; 
/*      */       try {
/* 1052 */         ensureOpen();
/* 1053 */       } catch (IOException iOException) {
/* 1054 */         throw new InternalError(iOException);
/*      */       } 
/* 1056 */       JarEntry jarEntry = this.jar.getJarEntry(param1String);
/* 1057 */       if (jarEntry != null)
/* 1058 */         return checkResource(param1String, param1Boolean, jarEntry); 
/* 1060 */       if (this.index == null)
/* 1061 */         return null; 
/* 1063 */       HashSet<String> hashSet = new HashSet();
/* 1064 */       return getResource(param1String, param1Boolean, hashSet);
/*      */     }
/*      */     
/*      */     Resource getResource(String param1String, boolean param1Boolean, Set<String> param1Set) {
/* 1079 */       byte b = 0;
/* 1080 */       LinkedList<String> linkedList = null;
/* 1085 */       if ((linkedList = this.index.get(param1String)) == null)
/* 1086 */         return null; 
/*      */       while (true) {
/* 1089 */         int i = linkedList.size();
/* 1090 */         String[] arrayOfString = linkedList.<String>toArray(new String[i]);
/* 1092 */         while (b < i) {
/*      */           JarLoader jarLoader;
/*      */           final URL url;
/* 1093 */           String str = arrayOfString[b++];
/*      */           try {
/* 1098 */             uRL = new URL(this.csu, str);
/* 1099 */             String str1 = URLUtil.urlNoFragString(uRL);
/* 1100 */             if ((jarLoader = (JarLoader)this.lmap.get(str1)) == null) {
/* 1104 */               jarLoader = AccessController.<JarLoader>doPrivileged(new PrivilegedExceptionAction<JarLoader>() {
/*      */                     public URLClassPath.JarLoader run() throws IOException {
/* 1107 */                       return new URLClassPath.JarLoader(url, URLClassPath.JarLoader.this.handler, URLClassPath.JarLoader.this
/* 1108 */                           .lmap, URLClassPath.JarLoader.this.acc);
/*      */                     }
/*      */                   }this.acc);
/* 1116 */               JarIndex jarIndex = jarLoader.getIndex();
/* 1117 */               if (jarIndex != null) {
/* 1118 */                 int j = str.lastIndexOf("/");
/* 1119 */                 jarIndex.merge(this.index, (j == -1) ? null : str
/* 1120 */                     .substring(0, j + 1));
/*      */               } 
/* 1124 */               this.lmap.put(str1, jarLoader);
/*      */             } 
/* 1126 */           } catch (PrivilegedActionException privilegedActionException) {
/*      */             continue;
/* 1128 */           } catch (MalformedURLException malformedURLException) {
/*      */             continue;
/*      */           } 
/* 1136 */           boolean bool = !param1Set.add(URLUtil.urlNoFragString(uRL)) ? true : false;
/* 1137 */           if (!bool) {
/*      */             try {
/* 1139 */               jarLoader.ensureOpen();
/* 1140 */             } catch (IOException iOException) {
/* 1141 */               throw new InternalError(iOException);
/*      */             } 
/* 1143 */             JarEntry jarEntry = jarLoader.jar.getJarEntry(param1String);
/* 1144 */             if (jarEntry != null)
/* 1145 */               return jarLoader.checkResource(param1String, param1Boolean, jarEntry); 
/* 1152 */             if (!jarLoader.validIndex(param1String))
/* 1154 */               throw new InvalidJarIndexException("Invalid index"); 
/*      */           } 
/* 1163 */           if (bool || jarLoader == this || jarLoader
/* 1164 */             .getIndex() == null)
/*      */             continue; 
/*      */           Resource resource;
/* 1170 */           if ((resource = jarLoader.getResource(param1String, param1Boolean, param1Set)) != null)
/* 1172 */             return resource; 
/*      */         } 
/* 1177 */         linkedList = this.index.get(param1String);
/* 1180 */         if (b >= linkedList.size())
/* 1181 */           return null; 
/*      */       } 
/*      */     }
/*      */     
/*      */     URL[] getClassPath() throws IOException {
/* 1189 */       if (this.index != null)
/* 1190 */         return null; 
/* 1193 */       if (this.metaIndex != null)
/* 1194 */         return null; 
/* 1197 */       ensureOpen();
/* 1198 */       parseExtensionsDependencies();
/* 1200 */       if (SharedSecrets.javaUtilJarAccess().jarFileHasClassPathAttribute(this.jar)) {
/* 1201 */         Manifest manifest = this.jar.getManifest();
/* 1202 */         if (manifest != null) {
/* 1203 */           Attributes attributes = manifest.getMainAttributes();
/* 1204 */           if (attributes != null) {
/* 1205 */             String str = attributes.getValue(Attributes.Name.CLASS_PATH);
/* 1206 */             if (str != null)
/* 1207 */               return parseClassPath(this.csu, str); 
/*      */           } 
/*      */         } 
/*      */       } 
/* 1212 */       return null;
/*      */     }
/*      */     
/*      */     private void parseExtensionsDependencies() throws IOException {
/* 1219 */       ExtensionDependency.checkExtensionsDependencies(this.jar);
/*      */     }
/*      */     
/*      */     private URL[] parseClassPath(URL param1URL, String param1String) throws MalformedURLException {
/* 1229 */       StringTokenizer stringTokenizer = new StringTokenizer(param1String);
/* 1230 */       URL[] arrayOfURL = new URL[stringTokenizer.countTokens()];
/* 1231 */       byte b = 0;
/* 1232 */       while (stringTokenizer.hasMoreTokens()) {
/* 1233 */         String str = stringTokenizer.nextToken();
/* 1234 */         URL uRL = URLClassPath.DISABLE_CP_URL_CHECK ? new URL(param1URL, str) : tryResolve(param1URL, str);
/* 1235 */         if (uRL != null) {
/* 1236 */           arrayOfURL[b] = uRL;
/* 1237 */           b++;
/*      */           continue;
/*      */         } 
/* 1239 */         if (URLClassPath.DEBUG_CP_URL_CHECK)
/* 1240 */           System.err.println("Class-Path entry: \"" + str + "\" ignored in JAR file " + param1URL); 
/*      */       } 
/* 1245 */       if (b == 0) {
/* 1246 */         arrayOfURL = null;
/* 1247 */       } else if (b != arrayOfURL.length) {
/* 1249 */         arrayOfURL = Arrays.<URL>copyOf(arrayOfURL, b);
/*      */       } 
/* 1251 */       return arrayOfURL;
/*      */     }
/*      */     
/*      */     static URL tryResolve(URL param1URL, String param1String) throws MalformedURLException {
/* 1255 */       if ("file".equalsIgnoreCase(param1URL.getProtocol()))
/* 1256 */         return tryResolveFile(param1URL, param1String); 
/* 1258 */       return tryResolveNonFile(param1URL, param1String);
/*      */     }
/*      */     
/*      */     static URL tryResolveFile(URL param1URL, String param1String) throws MalformedURLException {
/*      */       boolean bool;
/* 1276 */       int i = param1String.indexOf(':');
/* 1278 */       if (i >= 0) {
/* 1279 */         String str = param1String.substring(0, i);
/* 1280 */         bool = "file".equalsIgnoreCase(str);
/*      */       } else {
/* 1282 */         bool = true;
/*      */       } 
/* 1284 */       return bool ? new URL(param1URL, param1String) : null;
/*      */     }
/*      */     
/*      */     static URL tryResolveNonFile(URL param1URL, String param1String) throws MalformedURLException {
/* 1295 */       String str = param1String.replace(File.separatorChar, '/');
/* 1296 */       if (isRelative(str)) {
/* 1297 */         URL uRL = new URL(param1URL, str);
/* 1298 */         String str1 = param1URL.getPath();
/* 1299 */         String str2 = uRL.getPath();
/* 1300 */         int i = str1.lastIndexOf('/');
/* 1301 */         if (i == -1)
/* 1302 */           i = str1.length() - 1; 
/* 1304 */         if (str2.regionMatches(0, str1, 0, i + 1) && str2
/* 1305 */           .indexOf("..", i) == -1)
/* 1306 */           return uRL; 
/*      */       } 
/* 1309 */       return null;
/*      */     }
/*      */     
/*      */     static boolean isRelative(String param1String) {
/*      */       try {
/* 1317 */         return !URI.create(param1String).isAbsolute();
/* 1318 */       } catch (IllegalArgumentException illegalArgumentException) {
/* 1319 */         return false;
/*      */       } 
/*      */     }
/*      */   }
/*      */   
/*      */   private static class FileLoader extends Loader {
/*      */     private File dir;
/*      */     
/*      */     FileLoader(URL param1URL) throws IOException {
/* 1333 */       super(param1URL);
/* 1334 */       if (!"file".equals(param1URL.getProtocol()))
/* 1335 */         throw new IllegalArgumentException("url"); 
/* 1337 */       String str = param1URL.getFile().replace('/', File.separatorChar);
/* 1338 */       str = ParseUtil.decode(str);
/* 1339 */       this.dir = (new File(str)).getCanonicalFile();
/*      */     }
/*      */     
/*      */     URL findResource(String param1String, boolean param1Boolean) {
/* 1346 */       Resource resource = getResource(param1String, param1Boolean);
/* 1347 */       if (resource != null)
/* 1348 */         return resource.getURL(); 
/* 1350 */       return null;
/*      */     }
/*      */     
/*      */     Resource getResource(final String name, boolean param1Boolean) {
/*      */       try {
/*      */         final File file;
/* 1356 */         URL uRL2 = new URL(getBaseURL(), ".");
/* 1357 */         final URL url = new URL(getBaseURL(), ParseUtil.encodePath(name, false));
/* 1359 */         if (!uRL1.getFile().startsWith(uRL2.getFile()))
/* 1361 */           return null; 
/* 1364 */         if (param1Boolean)
/* 1365 */           URLClassPath.check(uRL1); 
/* 1368 */         if (name.indexOf("..") != -1) {
/* 1370 */           file = (new File(this.dir, name.replace('/', File.separatorChar))).getCanonicalFile();
/* 1371 */           if (!file.getPath().startsWith(this.dir.getPath()))
/* 1373 */             return null; 
/*      */         } else {
/* 1376 */           file = new File(this.dir, name.replace('/', File.separatorChar));
/*      */         } 
/* 1379 */         if (file.exists())
/* 1380 */           return new Resource() {
/*      */               public String getName() {
/* 1381 */                 return name;
/*      */               }
/*      */               
/*      */               public URL getURL() {
/* 1382 */                 return url;
/*      */               }
/*      */               
/*      */               public URL getCodeSourceURL() {
/* 1383 */                 return URLClassPath.FileLoader.this.getBaseURL();
/*      */               }
/*      */               
/*      */               public InputStream getInputStream() throws IOException {
/* 1385 */                 return new FileInputStream(file);
/*      */               }
/*      */               
/*      */               public int getContentLength() throws IOException {
/* 1387 */                 return (int)file.length();
/*      */               }
/*      */             }; 
/* 1390 */       } catch (Exception exception) {
/* 1391 */         return null;
/*      */       } 
/* 1393 */       return null;
/*      */     }
/*      */   }
/*      */ }


/* Location:              /home/houzhizhen/software/jdk/jdk1.8.0_301/jre/lib/rt.jar!/sun/misc/URLClassPath.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */