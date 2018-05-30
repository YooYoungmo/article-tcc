package ymyoo.payment.dto;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Date;

public class ParticipantLink {
    private URI uri;

    private LocalDateTime expires;

    public ParticipantLink() {
    }

    public ParticipantLink(URI uri, LocalDateTime expires) {
        this.uri = uri;
        this.expires = expires;
    }

    public URI getUri() {
        return uri;
    }

    public LocalDateTime getExpires() {
        return expires;
    }
}

