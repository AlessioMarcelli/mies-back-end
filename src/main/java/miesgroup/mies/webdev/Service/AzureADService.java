package miesgroup.mies.webdev.Service;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import miesgroup.mies.webdev.Model.Secret;
import miesgroup.mies.webdev.Repository.SecretRepo;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AzureADService {

    @Inject // ✅ Permette a Quarkus di iniettare il repository
    SecretRepo secretRepo;

    @ConfigProperty(name = "azure.client-id")
    String clientId;

    @ConfigProperty(name = "azure.tenant-id")
    String tenantId;

    private Secret clientSecret;

    @PostConstruct
    public void init() {
        if (secretRepo == null) {
            throw new RuntimeException("SecretRepo is not injected! Check your dependency injection.");
        }

        this.clientSecret = secretRepo.findAll().stream().findFirst().orElse(null);
        if (clientSecret == null) {
            throw new RuntimeException("Secret not found in database");
        }
        System.out.println("AzureADService initialized with secret: " + clientSecret.getSecret());
    }

    public String getPowerBIAccessToken() {
        System.out.println("clientId: " + clientId);
        System.out.println("tenantId: " + tenantId);
        System.out.println("clientSecret: " + (clientSecret != null ? clientSecret.getSecret() : "NULL"));

        if (clientSecret == null) {
            throw new RuntimeException("Client secret is null. Check database records.");
        }

        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret.getSecret())
                .tenantId(tenantId)
                .build();

        try {
            AccessToken token = credential.getToken(
                    new TokenRequestContext()
                            .addScopes("https://analysis.windows.net/powerbi/api/.default")
            ).block();

            if (token == null) {
                throw new RuntimeException("Failed to retrieve access token.");
            }

            System.out.println("Access token: " + token.getToken());
            return token.getToken();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting Power BI token: " + e.getMessage(), e);
        }
    }
}
