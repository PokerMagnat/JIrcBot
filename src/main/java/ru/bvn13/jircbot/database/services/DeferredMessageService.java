package ru.bvn13.jircbot.database.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bvn13.jircbot.database.entities.DeferredMessage;
import ru.bvn13.jircbot.database.repositories.DeferredMessageRepository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bvn13 on 31.01.2018.
 */
@Service
public class DeferredMessageService {

    @Autowired
    private DeferredMessageRepository deferredMessageRepository;

    public List<DeferredMessage> getDeferredMessagesForUser(String channelName, String user, String ident) {
        List<DeferredMessage> messagesByNickname = deferredMessageRepository.getDeferredMessagesByChannelNameAndRecipientAndSentOrderByDtCreated(channelName, user.toLowerCase(), false);
        List<DeferredMessage> messagesByIdent = deferredMessageRepository.getDeferredMessagesByChannelNameAndRecipientIdentAndSentOrderByDtCreated(channelName, ident, false);
        return Stream.concat(messagesByNickname.stream(), messagesByIdent.stream()).collect(Collectors.toList());
    }

    public void saveDeferredMessage(String channelName, String sender, String recipient, String message) {
        DeferredMessage msg = new DeferredMessage();
        msg.setChannelName(channelName);
        msg.setSender(sender);
        msg.setRecipient(recipient.toLowerCase());
        msg.setMessage(message);
        msg.setSent(false);
        deferredMessageRepository.save(msg);
    }

    public void markMessageWasSent(DeferredMessage msg) {
        msg.setSent(true);
        deferredMessageRepository.save(msg);
    }

    @Transactional
    public int forgetAllMessages(String channelName, String recipient, String ident) {
        AtomicInteger count = new AtomicInteger(0);
        List<DeferredMessage> messages = getDeferredMessagesForUser(channelName, recipient, ident);
        messages.forEach(msg -> {
            count.set(count.get()+1);
            msg.setSent(true);
            deferredMessageRepository.save(msg);
        });
        return count.get();
    }

}
