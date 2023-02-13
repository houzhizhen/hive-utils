package com.baidu.hive.authorizer;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HiveAccessControlException;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HiveAuthorizer;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HiveAuthzContext;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HiveAuthzPluginException;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HiveOperationType;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HivePolicyProvider;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HivePrincipal;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HivePrivilege;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HivePrivilegeInfo;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HivePrivilegeObject;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HiveRoleGrant;

import java.util.ArrayList;
import java.util.List;

public class RowFilterHiveAuthorizer implements HiveAuthorizer {

    private final String dbName;
    private final String tableName;
    private final String expression;

    public RowFilterHiveAuthorizer(HiveConf conf) {
        this.dbName = conf.get("rowfilter.db-name", "");
        this.tableName = conf.get("rowfilter.table-name", "");
        this.expression = conf.get("rowfilter.expression", "");
    }

    @Override
    public VERSION getVersion() {
        return VERSION.V1;
    }

    @Override
    public void grantPrivileges(List<HivePrincipal> hivePrincipals, List<HivePrivilege> hivePrivileges, HivePrivilegeObject hivePrivObject, HivePrincipal grantorPrincipal, boolean grantOption) throws HiveAuthzPluginException, HiveAccessControlException {

    }

    @Override
    public void revokePrivileges(List<HivePrincipal> hivePrincipals, List<HivePrivilege> hivePrivileges, HivePrivilegeObject hivePrivObject, HivePrincipal grantorPrincipal, boolean grantOption) throws HiveAuthzPluginException, HiveAccessControlException {

    }

    @Override
    public void createRole(String roleName, HivePrincipal adminGrantor) throws HiveAuthzPluginException, HiveAccessControlException {

    }

    @Override
    public void dropRole(String roleName) throws HiveAuthzPluginException, HiveAccessControlException {

    }

    @Override
    public List<HiveRoleGrant> getPrincipalGrantInfoForRole(String roleName) throws HiveAuthzPluginException, HiveAccessControlException {
        return null;
    }

    @Override
    public List<HiveRoleGrant> getRoleGrantInfoForPrincipal(HivePrincipal principal) throws HiveAuthzPluginException, HiveAccessControlException {
        return null;
    }

    @Override
    public void grantRole(List<HivePrincipal> hivePrincipals, List<String> roles, boolean grantOption, HivePrincipal grantorPrinc) throws HiveAuthzPluginException, HiveAccessControlException {

    }

    @Override
    public void revokeRole(List<HivePrincipal> hivePrincipals, List<String> roles, boolean grantOption, HivePrincipal grantorPrinc) throws HiveAuthzPluginException, HiveAccessControlException {

    }

    @Override
    public void checkPrivileges(HiveOperationType hiveOpType, List<HivePrivilegeObject> inputsHObjs, List<HivePrivilegeObject> outputHObjs, HiveAuthzContext context) throws HiveAuthzPluginException, HiveAccessControlException {

    }

    @Override
    public List<HivePrivilegeObject> filterListCmdObjects(List<HivePrivilegeObject> listObjs, HiveAuthzContext context) throws HiveAuthzPluginException, HiveAccessControlException {
        return null;
    }

    @Override
    public List<String> getAllRoles() throws HiveAuthzPluginException, HiveAccessControlException {
        return null;
    }

    @Override
    public List<HivePrivilegeInfo> showPrivileges(HivePrincipal principal, HivePrivilegeObject privObj) throws HiveAuthzPluginException, HiveAccessControlException {
        return null;
    }

    @Override
    public void setCurrentRole(String roleName) throws HiveAccessControlException, HiveAuthzPluginException {

    }

    @Override
    public List<String> getCurrentRoleNames() throws HiveAuthzPluginException {
        return null;
    }

    @Override
    public void applyAuthorizationConfigPolicy(HiveConf hiveConf) throws HiveAuthzPluginException {

    }

    @Override
    public Object getHiveAuthorizationTranslator() throws HiveAuthzPluginException {
        return null;
    }

    @Override
    public List<HivePrivilegeObject> applyRowFilterAndColumnMasking(
            HiveAuthzContext context, List<HivePrivilegeObject> privObjs) {
            for (HivePrivilegeObject hiveObj : privObjs) {
                HivePrivilegeObject.HivePrivilegeObjectType hiveObjType = hiveObj.getType();

                if(hiveObjType == HivePrivilegeObject.HivePrivilegeObjectType.TABLE_OR_VIEW) {
                    String tableName = hiveObj.getObjectName();
                    if (this.dbName.equals(hiveObj.getDbname()) && this.tableName.equals(tableName)) {
                        List<HivePrivilegeObject> ret = new ArrayList<>();
                        hiveObj.setRowFilterExpression(this.expression);
                        ret.add(hiveObj);
                        return ret;
                    }
                }
            }
        return privObjs;
    }

    @Override
    public boolean needTransform() {
        return true;
    }

    @Override
    public HivePolicyProvider getHivePolicyProvider() throws HiveAuthzPluginException {
        return null;
    }
}
