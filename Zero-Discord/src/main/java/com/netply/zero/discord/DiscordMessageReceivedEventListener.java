package com.netply.zero.discord;

import sx.blah.discord.api.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

class DiscordMessageReceivedEventListener implements IListener<MessageReceivedEvent> {
    public void handle(MessageReceivedEvent messageReceivedEvent) {

    }

//    private static final Logger log = Log.getLogger();
//    private ArrayList<NameFilterMessageHandler> messageHandlers = new ArrayList<>();
//
//
//    public DiscordMessageReceivedEventListener() {
//        messageHandlers.add(new HateMessageHandler());
//        messageHandlers.add(new HelloMessageHandler());
//        messageHandlers.add(new GreetingMessageHandler());
//        messageHandlers.add(new DropThatBaseMessageHandler());
//        messageHandlers.add(new GoodWorkMessageHandler());
//        messageHandlers.add(new GoAwayMessageHandler());
//        messageHandlers.add(new NekoChanMessageHandler());
//
//        messageHandlers.addAll(LeagueMessageHandlerProvider.getAllLeagueMessageHandlers());
//        messageHandlers.addAll(MangaMessageHandlerProvider.getAllMangaHandlers());
//        messageHandlers.addAll(SubtleMessageHandlerProvider.getSubtleHandlers());
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public void handle(MessageReceivedEvent event) {
//        String message = event.getMessage().getContent();
//        log.info(String.format("Discord message: [%s] %s", event.getMessage().getAuthor().getName(), message));
//        Database.getInstance().addToQueue(statement -> Database.logIncomingDiscordMessage(statement, event.getMessage()), null);
//        String cleanMessage = message.toLowerCase().replaceAll("[^a-zA-Z\\s-]", "").replaceAll("\\s+", " ");
//
//        messageHandlers.stream().filter(isMessageValid(message, cleanMessage))
//                .filter(whatsAppMessageHandler -> !(whatsAppMessageHandler.isNameRequired() && !event.getMessage().getChannel().isPrivate())
//                        || Self.containsName(message) || Self.containsName(event.getMessage()))
//                .forEach(messageHandler -> {
//                    log.info("Invoking message handler: " + messageHandler.getClass().getName());
//                    messageHandler.parse(new ReceivedDiscordMessage(event.getMessage(), event.getClient(), event.getMessage().getContent()));
//                });
//
//        MessageQueue.getInstance().add(new ReceivedDiscordMessage(event.getMessage(), event.getClient(), message));
//    }
//
//    private Predicate<NameFilterMessageHandler> isMessageValid(String message, String cleanMessage) {
//        return messageHandler -> {
//            if (messageHandler.isCleanMessageRequired()) {
//                return messageHandler.isValid(cleanMessage);
//            } else {
//                return messageHandler.isValid(message.toLowerCase());
//            }
//        };
//    }
}
