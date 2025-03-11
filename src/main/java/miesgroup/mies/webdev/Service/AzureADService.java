package miesgroup.mies.webdev.Service;

import com.azure.core.credential.AccessToken;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AzureADService {

    @ConfigProperty(name = "azure.client-id")
    String clientId;

    @ConfigProperty(name = "azure.client-secret")
    String clientSecret;

    @ConfigProperty(name = "azure.tenant-id")
    String tenantId;

    public String getPowerBIAccessToken() {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();

        AccessToken token = credential.getToken(
                new com.azure.core.credential.TokenRequestContext()
                        .addScopes("https://analysis.windows.net/powerbi/api/.default")
        ).block();

        return token.getToken();
    }
}


