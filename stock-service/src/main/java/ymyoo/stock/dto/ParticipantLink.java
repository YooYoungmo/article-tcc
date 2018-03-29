package ymyoo.stock.dto;

import java.net.URI;
import java.util.Date;

public class ParticipantLink {
    private URI uri;

    private Date expires;

    public ParticipantLink() {
    }

    public ParticipantLink(URI uri, Date expires) {
        this.uri = uri;
        this.expires = expires;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
}

