package ru.bvn13.jircbot.listeners;

import org.pircbotx.hooks.types.GenericMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bvn13.jircbot.bot.ImprovedListenerAdapter;
import ru.bvn13.jircbot.database.entities.GrammarCorrection;
import ru.bvn13.jircbot.database.services.ChannelSettingsService;
import ru.bvn13.jircbot.database.services.GrammarCorrectionService;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bvn13 on 03.02.2018.
 */
@Component
public class GrammarCorrectorListener extends ImprovedListenerAdapter {

    private static final String COMMAND = "?correct";

    private static SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ChannelSettingsService channelSettingsService;

    @Autowired
    private GrammarCorrectionService grammarCorrectionService;


    @Override
    public void onGenericMessage(final GenericMessageEvent event) throws Exception {

        if (!channelSettingsService.getChannelSettings(this.getChannelName(event)).getGrammarCorrectionEnabled()) {
            return;
        }

        if (event.getUser().getUserId().equals(event.getBot().getUserBot().getUserId())) {
            return;
        }

        if (event.getMessage().startsWith(COMMAND)) {
            onCommand(event);
        } else {
            checkForCorrection(event);
        }

    }


    private void checkForCorrection(final GenericMessageEvent event) throws Exception {
        String message = event.getMessage().replace(COMMAND, "").trim();
        List<String> corrections = grammarCorrectionService.getCorrectionsForMessage(message);
        corrections.forEach(correct -> {
            this.sendNotice(event,"*"+correct);
        });
    }

    private void onCommand(final GenericMessageEvent event) throws Exception {
        String message = event.getMessage().replace(COMMAND, "").trim();
        String commands[] = message.split(" ", 2);
        if (commands.length == 2) {
            if (commands[0].trim().equalsIgnoreCase("add")) {
                String params[] = commands[1].trim().split(">");
                if (params.length != 2) {
                    event.respond(helpMessage());
                } else {
                    grammarCorrectionService.saveGrammarCorrection(params[0].trim(), params[1].trim(), event.getUser().getNick());
                    event.respond("added correction: "+params[0].trim()+" > "+params[1].trim());
                }
            } else if (commands[1].trim().equalsIgnoreCase("remove")) {
                String params[] = commands[1].trim().split(">");
                if (grammarCorrectionService.removeCorrection(params[0].trim(), params[1].trim())) {
                    event.respond("added correction: "+params[0].trim()+" > "+params[1].trim());
                } else {
                    event.respond("correction not found: "+params[0].trim()+" > "+params[1].trim());
                }
            } else {
                event.respond(helpMessage());
            }
        } else if (commands.length == 1) {
            if (commands[0].trim().equalsIgnoreCase("help")) {
                event.respond(helpMessage());
            } else if (commands[0].trim().equalsIgnoreCase("show")) {
                List<GrammarCorrection> corrections = grammarCorrectionService.getAllCorrections();
                if (corrections.size() > 0) {
                    event.respond("sent in private");
                    AtomicReference<Integer> i = new AtomicReference<>(0);
                    corrections.forEach(c -> {
                        i.set(i.get()+1);
                        event.respondPrivateMessage(""+i.get()+": "+c.getWord()+" > "+c.getCorrection()+" / by "+c.getAuthor()+" at "+dt.format(c.getDtUpdated()));
                    });
                } else {
                    event.respond("correction table is empty");
                }
            }
        } else {
            event.respondWith(helpMessage());
        }

    }

    private String helpMessage() {
        return "syntax: ?correct add <REGEX-formatted word> > <full correction> | ?correct remove <REGEX-formatted word> > <full correction> | ?correct show";
    }

}
