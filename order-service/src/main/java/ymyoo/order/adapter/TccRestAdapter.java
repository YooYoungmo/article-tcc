package ymyoo.order.adapter;

import java.util.List;

public interface TccRestAdapter {
    List<ParticipantLink> doTry(List<ParticipationRequest> participationRequests);
    void confirmAll(List<ParticipantLink> participantLinks);
    void cancelAll(List<ParticipantLink> participantLinks);
}
