package org.wso2.sample.user.store.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

/**
 * Sample JDBC User Store Manager Class used for renaming existing users in the userstore.
 */
public class CustomUserStoreManager extends JDBCUserStoreManager {


    private static Log log = LogFactory.getLog(CustomUserStoreManager.class);
    private String userNameRenameClaimUri = CustomUserStoreConstants.NEW_USERNAME_DEFAULT_CLAIM_URI;


    public CustomUserStoreManager() {

    }

    public CustomUserStoreManager(org.wso2.carbon.user.api.RealmConfiguration realmConfig,
                                  Map<String, Object> properties,
                                  ClaimManager claimManager,
                                  ProfileConfigurationManager profileManager,
                                  UserRealm realm, Integer tenantId)
            throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, false);

        if(realmConfig.getUserStoreProperty(CustomUserStoreConstants.USERNAME_RENAME_CLAIM) != null){
            userNameRenameClaimUri = realmConfig.getUserStoreProperty(CustomUserStoreConstants.USERNAME_RENAME_CLAIM);
        }

    }

    @Override public void doSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        if (userNameRenameClaimUri.equals(claimURI)) {

            // Handle with special flow

            String newUserName = claimValue;

            if (!userName.equals(newUserName)) {

                if (log.isDebugEnabled()) {
                    log.debug("Trying to rename user " + userName + " to " + claimValue);
                }

                // Check if new username is already existing
                if (!isExistingUser(newUserName)) {
                    updateUsername(userName, newUserName, tenantId);

                    if (log.isDebugEnabled()) {
                        log.debug("Username is successfully updated from " + userName + " to " + newUserName);
                    }

                    // Make new username as the current username
                    userName = newUserName;

                    // Add SCIM username claim URI
                    claimURI = CustomUserStoreConstants.SCIM_USERNAME_CLAIM;

                } else {

                    String errorMessage =
                            "Cannot rename user " + userName + " to " + newUserName + " as " + newUserName +
                            " is already existing";

                    log.error(errorMessage);

                    throw new CustomUserStoreManagerException(
                            CustomUserStoreConstants.USERNAME_ALREADY_EXISTING_ERROR_CODE, errorMessage);
                }
            }
        }

        super.doSetUserClaimValue(userName, claimURI, claimValue, profileName);

    }

    @Override public void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {

            if (claims.containsKey(userNameRenameClaimUri) && !claims.get(userNameRenameClaimUri).isEmpty()) {

                // Handle with special flow

                String newUserName = claims.get(userNameRenameClaimUri);

                if (!userName.equals(newUserName)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Trying to rename user " + userName + " to " + newUserName);
                    }

                    // Check if new username is already existing
                    if (!isExistingUser(newUserName)) {

                        // Remove the userName claim since remaing is already done
                        claims.remove(userNameRenameClaimUri);

                        // Add new username for SCIM username
                        claims.put(CustomUserStoreConstants.SCIM_USERNAME_CLAIM, newUserName);

                        // Update other claims
                        super.doSetUserClaimValues(userName, claims, profileName);

                        //updateUsername(userName, newUserName, tenantId);
                        doSetUserClaimValue(userName, userNameRenameClaimUri, newUserName, profileName);


                        if (log.isDebugEnabled()) {
                            log.debug("Username is successfully updated from " + userName + " to " + newUserName);
                        }
                    } else {

                        String errorMessage =
                                "Cannot rename user " + userName + " to " + newUserName + " as " + newUserName +
                                " is already existing";

                        log.error(errorMessage);

                        throw new CustomUserStoreManagerException(
                                CustomUserStoreConstants.USERNAME_ALREADY_EXISTING_ERROR_CODE, errorMessage);
                    }
                }
            } else {
                super.doSetUserClaimValues(userName, claims, profileName);
            }

    }

    @Override
    public Properties getDefaultUserStoreProperties(){

        Properties defaultUserStoreProperties = super.getDefaultUserStoreProperties();

        Property[] mandatoryProperties = concat(CustomUserStoreConstants.CUSTOM_UM_MANDATORY_PROPERTIES.toArray(
                new Property[CustomUserStoreConstants.CUSTOM_UM_MANDATORY_PROPERTIES.size()]),
                                                defaultUserStoreProperties.getMandatoryProperties());
        Property[] optionalProperties = concat(CustomUserStoreConstants.CUSTOM_UM_OPTIONAL_PROPERTIES.toArray
                (new Property[CustomUserStoreConstants.CUSTOM_UM_OPTIONAL_PROPERTIES.size()]),
                                               defaultUserStoreProperties.getOptionalProperties());
        Property[] advancedProperties = concat(CustomUserStoreConstants.CUSTOM_UM_ADVANCED_PROPERTIES.toArray
                (new Property[CustomUserStoreConstants.CUSTOM_UM_ADVANCED_PROPERTIES.size()]),
                                               defaultUserStoreProperties.getAdvancedProperties());

        Properties properties = new Properties();
        properties.setMandatoryProperties(mandatoryProperties);
        properties.setOptionalProperties(optionalProperties);
        properties.setAdvancedProperties(advancedProperties);
        return properties;
    }

    private void updateUsername(String userName, String newUserName, int tenantId) throws UserStoreException {

        if(log.isDebugEnabled()){
            log.debug("Trying to rename the user " + userName + " to " + newUserName + " in the database");
        }

        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            PreparedStatement prepStmt = dbConnection.prepareStatement(CustomUserStoreConstants.UPDATE_USERNAME_SQL_STATEMENT);
            prepStmt.setString(1, newUserName);
            prepStmt.setString(2, userName);
            prepStmt.setInt(3, tenantId);

            prepStmt.executeUpdate();

            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage = "Error occurred while updating the username of : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    private static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


}
