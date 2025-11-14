package com.example.telegram;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.boost.ChatBoost;
import org.telegram.telegrambots.meta.api.objects.boost.ChatBoostRemoved;
import org.telegram.telegrambots.meta.api.objects.boost.ChatBoostSourceGiftCode;
import org.telegram.telegrambots.meta.api.objects.boost.ChatBoostUpdated;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.api.objects.inlinequery.ChosenInlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.payments.OrderInfo;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.ShippingAddress;
import org.telegram.telegrambots.meta.api.objects.payments.ShippingQuery;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionCountUpdated;
import org.telegram.telegrambots.meta.api.objects.reactions.MessageReactionUpdated;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionCount;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeCustomEmoji;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;

class UpdatePrinterTest {

    private final UpdatePrinter printer = new UpdatePrinter();

    @Test
    void describeHandlesNullUpdates() {
        String result = printer.describe(null);
        assertTrue(result.contains("Update <null>"));
    }

    @Test
    void describeListsAllKnownSections() {
        Update update = buildFullUpdate();
        String description = printer.describe(update);

        List<String> expectedLabels = List.of(
                "[MESSAGE]",
                "[EDITED_MESSAGE]",
                "[CHANNEL_POST]",
                "[EDITED_CHANNEL_POST]",
                "[INLINE_QUERY]",
                "[CHOSEN_INLINE_RESULT]",
                "[CALLBACK_QUERY]",
                "[SHIPPING_QUERY]",
                "[PRE_CHECKOUT_QUERY]",
                "[POLL]",
                "[POLL_ANSWER]",
                "[MY_CHAT_MEMBER]",
                "[CHAT_MEMBER]",
                "[CHAT_JOIN_REQUEST]",
                "[MESSAGE_REACTION]",
                "[MESSAGE_REACTION_COUNT]",
                "[CHAT_BOOST]",
                "[REMOVED_CHAT_BOOST]");

        for (String label : expectedLabels) {
            assertTrue(description.contains(label), () -> "Missing section " + label + "\n" + description);
        }
    }

    private static Update buildFullUpdate() {
        Update update = new Update();
        update.setUpdateId(777);
        update.setMessage(messageWithText(10, "Hello world"));
        update.setEditedMessage(messageWithText(11, "Edited"));
        update.setChannelPost(messageWithText(12, "Channel post"));
        update.setEditedChannelPost(messageWithText(13, "Edited channel"));
        update.setInlineQuery(inlineQuery());
        update.setChosenInlineQuery(chosenInlineQuery());
        update.setCallbackQuery(callbackQuery());
        update.setShippingQuery(shippingQuery());
        update.setPreCheckoutQuery(preCheckoutQuery());
        update.setPoll(poll());
        update.setPollAnswer(pollAnswer());
        update.setMyChatMember(chatMemberUpdated());
        update.setChatMember(chatMemberUpdated());
        update.setChatJoinRequest(chatJoinRequest());
        update.setMessageReaction(messageReaction());
        update.setMessageReactionCount(messageReactionCount());
        update.setChatBoost(chatBoostUpdated());
        update.setRemovedChatBoost(chatBoostRemoved());
        return update;
    }

    private static Message messageWithText(int messageId, String text) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setChat(sampleChat(100 + messageId, "supergroup", "Chat " + messageId));
        message.setFrom(user(1000 + messageId, "User" + messageId));
        message.setText(text);
        return message;
    }

    private static Chat sampleChat(long id, String type, String title) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setType(type);
        chat.setTitle(title);
        return chat;
    }

    private static User user(long id, String name) {
        User user = new User();
        user.setId(id);
        user.setFirstName(name);
        user.setUserName(name.toLowerCase());
        return user;
    }

    private static InlineQuery inlineQuery() {
        InlineQuery query = new InlineQuery();
        query.setId("inline-1");
        query.setFrom(user(2000, "Inline"));
        query.setQuery("search term");
        query.setOffset("5");
        query.setChatType("private");
        Location location = new Location();
        location.setLatitude(55.75);
        location.setLongitude(37.61);
        query.setLocation(location);
        return query;
    }

    private static ChosenInlineQuery chosenInlineQuery() {
        ChosenInlineQuery chosen = new ChosenInlineQuery();
        chosen.setResultId("result-1");
        chosen.setFrom(user(2001, "Chooser"));
        chosen.setQuery("search term");
        chosen.setInlineMessageId("inline-msg-1");
        Location location = new Location();
        location.setLatitude(40.71);
        location.setLongitude(-74.0);
        chosen.setLocation(location);
        return chosen;
    }

    private static CallbackQuery callbackQuery() {
        CallbackQuery callback = new CallbackQuery();
        callback.setId("cb-1");
        callback.setFrom(user(2002, "Callback"));
        callback.setData("data");
        callback.setChatInstance("chat-instance");
        callback.setMessage(messageWithText(20, "Callback message"));
        return callback;
    }

    private static ShippingQuery shippingQuery() {
        ShippingQuery query = new ShippingQuery();
        query.setId("ship-1");
        query.setFrom(user(2003, "Shipper"));
        query.setInvoicePayload("payload");
        query.setShippingAddress(shippingAddress());
        return query;
    }

    private static ShippingAddress shippingAddress() {
        ShippingAddress address = new ShippingAddress();
        address.setCountryCode("RU");
        address.setState("Moscow");
        address.setCity("Moscow");
        address.setStreetLine1("Tverskaya 1");
        address.setStreetLine2("Apt 1");
        address.setPostCode("101000");
        return address;
    }

    private static PreCheckoutQuery preCheckoutQuery() {
        PreCheckoutQuery query = new PreCheckoutQuery();
        query.setId("pre-1");
        query.setFrom(user(2004, "Buyer"));
        query.setCurrency("RUB");
        query.setTotalAmount(1000);
        query.setInvoicePayload("payload");
        query.setShippingOptionId("option-1");
        OrderInfo info = new OrderInfo();
        info.setName("Ivan");
        info.setPhoneNumber("+7000000000");
        info.setEmail("ivan@example.com");
        info.setShippingAddress(shippingAddress());
        query.setOrderInfo(info);
        return query;
    }

    private static Poll poll() {
        Poll poll = new Poll();
        poll.setId("poll-1");
        poll.setQuestion("Favourite color?");
        poll.setOptions(List.of(pollOption("blue", 10), pollOption("red", 5)));
        poll.setTotalVoterCount(15);
        poll.setAllowMultipleAnswers(true);
        poll.setType("regular");
        return poll;
    }

    private static PollOption pollOption(String text, int voters) {
        PollOption option = new PollOption();
        option.setText(text);
        option.setVoterCount(voters);
        return option;
    }

    private static PollAnswer pollAnswer() {
        PollAnswer answer = new PollAnswer();
        answer.setPollId("poll-1");
        answer.setUser(user(2005, "Voter"));
        answer.setOptionIds(Arrays.asList(0, 1));
        return answer;
    }

    private static ChatMemberUpdated chatMemberUpdated() {
        ChatMemberUpdated updated = new ChatMemberUpdated();
        updated.setChat(sampleChat(300, "group", "Moderated"));
        updated.setFrom(user(2006, "Moderator"));
        ChatMemberOwner oldMember = new ChatMemberOwner();
        oldMember.setUser(user(3000, "Owner"));
        ChatMemberAdministrator newMember = new ChatMemberAdministrator();
        newMember.setUser(user(3001, "Admin"));
        updated.setOldChatMember(oldMember);
        updated.setNewChatMember(newMember);
        ChatInviteLink inviteLink = new ChatInviteLink();
        inviteLink.setInviteLink("https://t.me/+invite");
        inviteLink.setCreator(user(2007, "Creator"));
        updated.setInviteLink(inviteLink);
        updated.setViaChatFolderInviteLink(true);
        return updated;
    }

    private static ChatJoinRequest chatJoinRequest() {
        ChatJoinRequest request = new ChatJoinRequest();
        request.setChat(sampleChat(400, "supergroup", "Requests"));
        request.setUser(user(2008, "Applicant"));
        request.setBio("Let me in");
        request.setInviteLink(new ChatInviteLink());
        return request;
    }

    private static MessageReactionUpdated messageReaction() {
        MessageReactionUpdated reaction = new MessageReactionUpdated();
        reaction.setChat(sampleChat(500, "supergroup", "Reactions"));
        reaction.setMessageId(50);
        reaction.setUser(user(2009, "Reactor"));
        reaction.setOldReaction(List.of(emojiReaction("👍")));
        reaction.setNewReaction(List.of(customReaction("custom-emoji-1")));
        return reaction;
    }

    private static MessageReactionCountUpdated messageReactionCount() {
        MessageReactionCountUpdated countUpdated = new MessageReactionCountUpdated();
        countUpdated.setChat(sampleChat(501, "supergroup", "Counts"));
        countUpdated.setMessageId(51);
        ReactionCount count = new ReactionCount();
        count.setType(customReaction("custom-emoji-2"));
        count.setTotalCount(3);
        countUpdated.setReactions(List.of(count));
        return countUpdated;
    }

    private static ReactionTypeEmoji emojiReaction(String emoji) {
        ReactionTypeEmoji reaction = new ReactionTypeEmoji();
        reaction.setType("emoji");
        reaction.setEmoji(emoji);
        return reaction;
    }

    private static ReactionTypeCustomEmoji customReaction(String id) {
        ReactionTypeCustomEmoji reaction = new ReactionTypeCustomEmoji();
        reaction.setType("custom_emoji");
        reaction.setCustomEmoji(id);
        return reaction;
    }

    private static ChatBoostUpdated chatBoostUpdated() {
        ChatBoostUpdated updated = new ChatBoostUpdated();
        updated.setChat(sampleChat(600, "supergroup", "Boosted"));
        ChatBoost boost = new ChatBoost();
        boost.setBoostId("boost-1");
        boost.setExpirationDate(123456);
        ChatBoostSourceGiftCode source = new ChatBoostSourceGiftCode();
        source.setSource("gift_code");
        source.setUser(user(2010, "Sponsor"));
        boost.setSource(source);
        updated.setBoost(boost);
        return updated;
    }

    private static ChatBoostRemoved chatBoostRemoved() {
        ChatBoostRemoved removed = new ChatBoostRemoved();
        removed.setChat(sampleChat(601, "supergroup", "Boosted"));
        removed.setBoostId("boost-1");
        removed.setRemoveDate(123500);
        ChatBoostSourceGiftCode source = new ChatBoostSourceGiftCode();
        source.setSource("gift_code");
        source.setUser(user(2011, "Sponsor"));
        removed.setSource(source);
        return removed;
    }
}
