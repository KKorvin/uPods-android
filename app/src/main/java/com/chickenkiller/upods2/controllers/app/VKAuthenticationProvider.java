package com.chickenkiller.upods2.controllers.app;

import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.regions.Regions;
import com.chickenkiller.upods2.controllers.internet.BackendManager;
import com.chickenkiller.upods2.utils.Logger;
import com.chickenkiller.upods2.utils.ServerApi;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

import org.json.JSONObject;

/**
 * Created by alonzilberman on 12/3/15.
 */
public class VKAuthenticationProvider extends AWSAbstractCognitoDeveloperIdentityProvider {

    private static final String DEVELOPER_PROVIDER_NAME = "login.vk.com";
    private static final String LOG = "VKAuthenticationProvider";
    private static final Regions REGION = Regions.US_EAST_1;
    public static final String PREF_IDENTITY_ID = "cognito_identity_id";

    public VKAuthenticationProvider(String accountId, String identityPoolId, Regions region) {
        super(accountId, identityPoolId, region);
    }

    // Return the developer provider name which you choose while setting up the
    // identity pool in the Amazon Cognito Console

    @Override
    public String getProviderName() {
        return DEVELOPER_PROVIDER_NAME;
    }

    /*
     * (non-Javadoc)
     * @see com.amazonaws.auth.AWSCognitoIdentityProvider#refresh() In refresh
     * method, you will have two flows:
     */
    /*
     * 1. When the app user uses developer authentication . In this case, make
     * the call to your developer backend, from where call the
     * GetOpenIdTokenForDeveloperIdentity API of Amazon Cognito service. For
     * this sample the GetToken request to the sample Cognito developer
     * authentication application is made. Be sure to call update(), so as to
     * set the identity id and the token received.
     */
    /*
     * 2.When the app user is not using the developer authentication, just call
     * the refresh method of the AWSAbstractCognitoDeveloperIdentityProvider
     * class which actually calls GetId and GetOpenIDToken API of Amazon
     * Cognito.
     */
    @Override
    public String refresh() {
        setToken(null);
        if (VKSdk.isLoggedIn() && getProviderName() != null && !this.loginsMap.isEmpty()
                && this.loginsMap.containsKey(getProviderName())) {
            RequestBody formBody = new FormEncodingBuilder()
                    .add("identityId", getIdentityId())
                    .add("devUserIdentifier", VKAccessToken.currentToken().accessToken)
                    .build();
            Request request = new Request.Builder()
                    .url(ServerApi.COGNITO_LOGIN).post(formBody)
                    .build();
            try {
                JSONObject jResponse = BackendManager.getInstance().sendSynchronicRequest(request);
                String responseToken = jResponse.getString("Token");
                String identityId = jResponse.getString("IdentityId");
                update(identityId, responseToken);
                return responseToken;
            } catch (Exception e) {
                Logger.printError(LOG, "Error in refresh: ");
                e.printStackTrace();
                this.getIdentityId();
                return null;
            }
        } else {
            this.getIdentityId();
            return null;
        }

    }

    /*
     * (non-Javadoc)
     * @see com.amazonaws.auth.AWSBasicCognitoIdentityProvider#getIdentityId()
     */
    /*
     * This method again has two flows as mentioned above depending on whether
     * the app user is using developer authentication or not. When using
     * developer authentication system, the identityId should be retrieved from
     * the developer backend. In the other case the identityId will be retrieved
     * using the getIdentityId() method which in turn calls Cognito GetId and
     * GetOpenIdToken APIs.
     */
    @Override
    public String getIdentityId() {
        identityId = LoginMaster.getInstance().getCredentialsProvider().getCachedIdentityId();
        if (identityId == null) {
            identityId = super.getIdentityId();
        }
        return identityId;
    }
}
