/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.sample.user.store.manager;


import org.wso2.carbon.user.api.Property;

import java.util.ArrayList;

public class CustomUserStoreConstants {


    //Properties for Read Active Directory User Store Manager
    public static final ArrayList<Property> CUSTOM_UM_MANDATORY_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> CUSTOM_UM_OPTIONAL_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> CUSTOM_UM_ADVANCED_PROPERTIES = new ArrayList<Property>();

    //DB Access Constants
    public static final String UPDATE_USERNAME_SQL_STATEMENT = "UPDATE UM_USER SET UM_USER_NAME= ? WHERE UM_USER_NAME= ? AND UM_TENANT_ID=?";

    //Properties for Claims
    public static final String USERNAME_RENAME_CLAIM = "UserNameRenameClaim";
    public static final String NEW_USERNAME_DEFAULT_CLAIM_URI = "http://wso2.org/claims/userName";
    public static final String SCIM_USERNAME_CLAIM = "urn:scim:schemas:core:1.0:userName";

    //Error Code Constants
    public static final int USERNAME_ALREADY_EXISTING_ERROR_CODE = 15300; //Change the error code according to the requirement

    static {

        //Set Optional Properties
        setProperty(USERNAME_RENAME_CLAIM,"Username Rename Claim URI", NEW_USERNAME_DEFAULT_CLAIM_URI, "Claim URI for renaming usernames");
    }


    private static void setProperty(String name, String displayName, String value, String description) {
        Property property = new Property(name, value, displayName + "#" +description, null);
        CUSTOM_UM_OPTIONAL_PROPERTIES.add(property);

    }

    private static void setMandatoryProperty(String name, String displayName, String value, String description) {
        Property property = new Property(name, value, displayName + "#" +description, null);
        CUSTOM_UM_MANDATORY_PROPERTIES.add(property);

    }

    private static void setAdvancedProperty(String name, String displayName, String value, String description) {
        Property property = new Property(name, value, displayName + "#" +description, null);
        CUSTOM_UM_ADVANCED_PROPERTIES.add(property);

    }

}
