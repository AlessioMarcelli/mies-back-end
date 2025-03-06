package miesgroup.mies.webdev.Rest.Exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // Se l'eccezione è di tipo NotYourPodException,
        // usiamo il metodo dedicato
        if (exception instanceof NotYourPodException) {
            return handleNotYourPodException((NotYourPodException) exception);
        }

        if (exception instanceof PodNotFound) {
            return handlePodNotFound((PodNotFound) exception);
        }


        // Altrimenti, gestiamo qualunque altra eccezione come errore interno 500
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                exception.getMessage() != null ? exception.getMessage() : "Si è verificato un errore interno"
        );

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }

    // Metodo privato per gestire PodNotFound
    private Response handlePodNotFound(PodNotFound exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                "POD_NOT_FOUND",
                exception.getMessage() != null ? exception.getMessage() : "Il POD richiesto non esiste"
        );

        return Response
                .status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }

    // Metodo privato per gestire NotYourPodException
    private Response handleNotYourPodException(NotYourPodException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                "NOT_YOUR_POD",
                exception.getMessage() != null ? exception.getMessage() : "Non hai i permessi per modificare questo POD"
        );

        return Response
                .status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}

