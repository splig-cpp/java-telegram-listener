package com.example.telegram;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
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
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionType;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeCustomEmoji;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;
import org.telegram.telegrambots.meta.api.objects.boost.ChatBoost;
import org.telegram.telegrambots.meta.api.objects.boost.ChatBoostRemoved;
import org.telegram.telegrambots.meta.api.objects.boost.ChatBoostSource;
import org.telegram.telegrambots.meta.api.objects.boost.ChatBoostUpdated;

/**
 * Converts {@link Update} objects into a concise, human readable diagnostic string so we can
 * quickly see which Telegram event(s) fired.
 */
public class UpdatePrinter {

    /**
     * Builds a multi-line description of every payload that is present in the given update.
     */
    public String describe(Update update) {
        if (update == null) {
            return "=== Update <null> ===\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Update #")
                .append(update.getUpdateId() == null ? "?" : update.getUpdateId())
                .append(" ===\n");

        int sections = 0;
        if (appendMessage(sb, "MESSAGE", update.getMessage())) sections++;
        if (appendMessage(sb, "EDITED_MESSAGE", update.getEditedMessage())) sections++;
        if (appendMessage(sb, "CHANNEL_POST", update.getChannelPost())) sections++;
        if (appendMessage(sb, "EDITED_CHANNEL_POST", update.getEditedChannelPost())) sections++;
        if (appendInlineQuery(sb, update.getInlineQuery())) sections++;
        if (appendChosenInlineQuery(sb, update.getChosenInlineQuery())) sections++;
        if (appendCallbackQuery(sb, update.getCallbackQuery())) sections++;
        if (appendShippingQuery(sb, update.getShippingQuery())) sections++;
        if (appendPreCheckoutQuery(sb, update.getPreCheckoutQuery())) sections++;
        if (appendPoll(sb, update.getPoll())) sections++;
        if (appendPollAnswer(sb, update.getPollAnswer())) sections++;
        if (appendChatMemberUpdate(sb, "MY_CHAT_MEMBER", update.getMyChatMember())) sections++;
        if (appendChatMemberUpdate(sb, "CHAT_MEMBER", update.getChatMember())) sections++;
        if (appendChatJoinRequest(sb, update.getChatJoinRequest())) sections++;
        if (appendMessageReaction(sb, update.getMessageReaction())) sections++;
        if (appendMessageReactionCount(sb, update.getMessageReactionCount())) sections++;
        if (appendChatBoost(sb, update.getChatBoost())) sections++;
        if (appendChatBoostRemoved(sb, update.getRemovedChatBoost())) sections++;

        if (sections == 0) {
            sb.append("No recognized payloads in this update.\n");
        }

        sb.append("=============================\n");
        return sb.toString();
    }

    /**
     * Convenience helper that prints the formatted update to stdout.
     */
    public void print(Update update) {
        System.out.print(describe(update));
    }

    private boolean appendMessage(StringBuilder sb, String label, Message message) {
        if (message == null) {
            return false;
        }
        sb.append('[').append(label).append("] id=")
                .append(valueOrUnknown(message.getMessageId()))
                .append(", chat=").append(formatChat(message.getChat()))
                .append(", from=").append(formatUser(message.getFrom()));
        if (message.hasText()) {
            sb.append(", text=\"").append(trimmed(message.getText())).append('\"');
        } else if (message.getCaption() != null) {
            sb.append(", caption=\"").append(trimmed(message.getCaption())).append('\"');
        } else {
            sb.append(", type=").append(detectMessagePayload(message));
        }
        if (Boolean.TRUE.equals(message.getIsAutomaticForward())) {
            sb.append(", automaticForward=true");
        }
        sb.append('\n');
        return true;
    }

    private boolean appendInlineQuery(StringBuilder sb, InlineQuery inlineQuery) {
        if (inlineQuery == null) {
            return false;
        }
        sb.append("[INLINE_QUERY] id=").append(valueOrUnknown(inlineQuery.getId()))
                .append(", from=").append(formatUser(inlineQuery.getFrom()))
                .append(", query=\"").append(trimmed(inlineQuery.getQuery())).append('\"')
                .append(", offset=").append(valueOrUnknown(inlineQuery.getOffset()))
                .append(", chatType=").append(valueOrUnknown(inlineQuery.getChatType()));
        Location location = inlineQuery.getLocation();
        if (location != null) {
            sb.append(", location=")
                    .append(formatCoordinate(location.getLatitude(), location.getLongitude()));
        }
        sb.append('\n');
        return true;
    }

    private boolean appendChosenInlineQuery(StringBuilder sb, ChosenInlineQuery chosen) {
        if (chosen == null) {
            return false;
        }
        sb.append("[CHOSEN_INLINE_RESULT] resultId=")
                .append(valueOrUnknown(chosen.getResultId()))
                .append(", from=").append(formatUser(chosen.getFrom()))
                .append(", query=\"").append(trimmed(chosen.getQuery())).append('\"')
                .append(", inlineMessageId=").append(valueOrUnknown(chosen.getInlineMessageId()));
        if (chosen.getLocation() != null) {
            sb.append(", location=")
                    .append(formatCoordinate(
                            chosen.getLocation().getLatitude(),
                            chosen.getLocation().getLongitude()));
        }
        sb.append('\n');
        return true;
    }

    private boolean appendCallbackQuery(StringBuilder sb, CallbackQuery callbackQuery) {
        if (callbackQuery == null) {
            return false;
        }
        sb.append("[CALLBACK_QUERY] id=").append(valueOrUnknown(callbackQuery.getId()))
                .append(", from=").append(formatUser(callbackQuery.getFrom()))
                .append(", chatInstance=")
                .append(valueOrUnknown(callbackQuery.getChatInstance()))
                .append(", data=")
                .append(valueOrUnknown(callbackQuery.getData()))
                .append(", gameShortName=")
                .append(valueOrUnknown(callbackQuery.getGameShortName()));
        sb.append('\n');
        return true;
    }

    private boolean appendShippingQuery(StringBuilder sb, ShippingQuery shippingQuery) {
        if (shippingQuery == null) {
            return false;
        }
        sb.append("[SHIPPING_QUERY] id=")
                .append(valueOrUnknown(shippingQuery.getId()))
                .append(", from=").append(formatUser(shippingQuery.getFrom()))
                .append(", invoicePayload=")
                .append(valueOrUnknown(shippingQuery.getInvoicePayload()));
        ShippingAddress address = shippingQuery.getShippingAddress();
        if (address != null) {
            sb.append(", address=")
                    .append(formatAddress(address));
        }
        sb.append('\n');
        return true;
    }

    private boolean appendPreCheckoutQuery(StringBuilder sb, PreCheckoutQuery query) {
        if (query == null) {
            return false;
        }
        sb.append("[PRE_CHECKOUT_QUERY] id=")
                .append(valueOrUnknown(query.getId()))
                .append(", from=").append(formatUser(query.getFrom()))
                .append(", totalAmount=")
                .append(valueOrUnknown(query.getTotalAmount()))
                .append(' ')
                .append(valueOrUnknown(query.getCurrency()))
                .append(", invoicePayload=")
                .append(valueOrUnknown(query.getInvoicePayload()))
                .append(", shippingOptionId=")
                .append(valueOrUnknown(query.getShippingOptionId()));
        OrderInfo info = query.getOrderInfo();
        if (info != null) {
            sb.append(", orderInfo=")
                    .append(formatOrderInfo(info));
        }
        sb.append('\n');
        return true;
    }

    private boolean appendPoll(StringBuilder sb, Poll poll) {
        if (poll == null) {
            return false;
        }
        sb.append("[POLL] id=").append(valueOrUnknown(poll.getId()))
                .append(", question=\"").append(trimmed(poll.getQuestion())).append('\"')
                .append(", totalVoters=")
                .append(valueOrUnknown(poll.getTotalVoterCount()))
                .append(", allowsMultiple=")
                .append(valueOrUnknown(poll.getAllowMultipleAnswers()))
                .append(", type=").append(valueOrUnknown(poll.getType()))
                .append(", options=")
                .append(formatPollOptions(poll.getOptions()));
        sb.append('\n');
        return true;
    }

    private boolean appendPollAnswer(StringBuilder sb, PollAnswer pollAnswer) {
        if (pollAnswer == null) {
            return false;
        }
        sb.append("[POLL_ANSWER] pollId=")
                .append(valueOrUnknown(pollAnswer.getPollId()))
                .append(", voter=").append(formatUser(pollAnswer.getUser()))
                .append(", optionIds=")
                .append(pollAnswer.getOptionIds() == null ? "[]" : pollAnswer.getOptionIds());
        sb.append('\n');
        return true;
    }

    private boolean appendChatMemberUpdate(StringBuilder sb, String label, ChatMemberUpdated payload) {
        if (payload == null) {
            return false;
        }
        sb.append('[').append(label).append("] chat=")
                .append(formatChat(payload.getChat()))
                .append(", actor=").append(formatUser(payload.getFrom()))
                .append(", oldStatus=")
                .append(describeChatMember(payload.getOldChatMember()))
                .append(", newStatus=")
                .append(describeChatMember(payload.getNewChatMember()));
        if (Boolean.TRUE.equals(payload.getViaChatFolderInviteLink())) {
            sb.append(", viaFolderInvite=true");
        }
        if (payload.getInviteLink() != null) {
            sb.append(", inviteLink=")
                    .append(formatInviteLink(payload.getInviteLink()));
        }
        sb.append('\n');
        return true;
    }

    private boolean appendChatJoinRequest(StringBuilder sb, ChatJoinRequest request) {
        if (request == null) {
            return false;
        }
        sb.append("[CHAT_JOIN_REQUEST] chat=")
                .append(formatChat(request.getChat()))
                .append(", user=").append(formatUser(request.getUser()))
                .append(", bio=")
                .append(valueOrUnknown(request.getBio()))
                .append(", inviteLink=")
                .append(request.getInviteLink() == null ? "none" : formatInviteLink(request.getInviteLink()));
        sb.append('\n');
        return true;
    }

    private boolean appendMessageReaction(StringBuilder sb, MessageReactionUpdated reaction) {
        if (reaction == null) {
            return false;
        }
        sb.append("[MESSAGE_REACTION] chat=")
                .append(formatChat(reaction.getChat()))
                .append(", messageId=")
                .append(valueOrUnknown(reaction.getMessageId()))
                .append(", actor=")
                .append(reaction.getUser() != null ? formatUser(reaction.getUser()) : formatChat(reaction.getActorChat()))
                .append(", old=")
                .append(formatReactionTypes(reaction.getOldReaction()))
                .append(", new=")
                .append(formatReactionTypes(reaction.getNewReaction()));
        sb.append('\n');
        return true;
    }

    private boolean appendMessageReactionCount(StringBuilder sb, MessageReactionCountUpdated reactionCount) {
        if (reactionCount == null) {
            return false;
        }
        sb.append("[MESSAGE_REACTION_COUNT] chat=")
                .append(formatChat(reactionCount.getChat()))
                .append(", messageId=")
                .append(valueOrUnknown(reactionCount.getMessageId()))
                .append(", total=")
                .append(formatReactionCounts(reactionCount.getReactions()));
        sb.append('\n');
        return true;
    }

    private boolean appendChatBoost(StringBuilder sb, ChatBoostUpdated updated) {
        if (updated == null) {
            return false;
        }
        ChatBoost boost = updated.getBoost();
        sb.append("[CHAT_BOOST] chat=")
                .append(formatChat(updated.getChat()))
                .append(", boostId=")
                .append(boost == null ? "unknown" : valueOrUnknown(boost.getBoostId()))
                .append(", source=")
                .append(formatBoostSource(boost == null ? null : boost.getSource()));
        if (boost != null) {
            sb.append(", expiresAt=")
                    .append(valueOrUnknown(boost.getExpirationDate()));
        }
        sb.append('\n');
        return true;
    }

    private boolean appendChatBoostRemoved(StringBuilder sb, ChatBoostRemoved removed) {
        if (removed == null) {
            return false;
        }
        sb.append("[REMOVED_CHAT_BOOST] boostId=")
                .append(valueOrUnknown(removed.getBoostId()))
                .append(", chat=")
                .append(formatChat(removed.getChat()))
                .append(", removedAt=")
                .append(valueOrUnknown(removed.getRemoveDate()))
                .append(", source=")
                .append(formatBoostSource(removed.getSource()));
        sb.append('\n');
        return true;
    }

    private String describeChatMember(org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember chatMember) {
        if (chatMember == null) {
            return "unknown";
        }
        return chatMember.getClass().getSimpleName() + "(" + chatMember.getStatus() + ")";
    }

    private String formatInviteLink(ChatInviteLink inviteLink) {
        return valueOrUnknown(inviteLink.getInviteLink())
                + " (creator=" + formatUser(inviteLink.getCreator()) + ")";
    }

    private String formatOrderInfo(OrderInfo orderInfo) {
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        joiner.add("name=" + valueOrUnknown(orderInfo.getName()));
        joiner.add("phone=" + valueOrUnknown(orderInfo.getPhoneNumber()));
        joiner.add("email=" + valueOrUnknown(orderInfo.getEmail()));
        if (orderInfo.getShippingAddress() != null) {
            joiner.add("address=" + formatAddress(orderInfo.getShippingAddress()));
        }
        return joiner.toString();
    }

    private String formatAddress(ShippingAddress address) {
        if (address == null) {
            return "unknown address";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(valueOrUnknown(address.getCountryCode()))
                .append(", ")
                .append(valueOrUnknown(address.getState()))
                .append(", ")
                .append(valueOrUnknown(address.getCity()))
                .append(", ")
                .append(valueOrUnknown(address.getStreetLine1()));
        if (address.getStreetLine2() != null && !address.getStreetLine2().isBlank()) {
            builder.append(' ')
                    .append(address.getStreetLine2().trim());
        }
        builder.append(", ")
                .append(valueOrUnknown(address.getPostCode()));
        return builder.toString();
    }

    private String formatPollOptions(List<PollOption> options) {
        if (options == null || options.isEmpty()) {
            return "[]";
        }
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (PollOption option : options) {
            if (option == null) {
                continue;
            }
            joiner.add(option.getText() + "=" + option.getVoterCount());
        }
        return joiner.toString();
    }

    private String formatReactionTypes(List<ReactionType> reactions) {
        if (reactions == null || reactions.isEmpty()) {
            return "[]";
        }
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (ReactionType reaction : reactions) {
            if (reaction == null) {
                continue;
            }
            if (reaction instanceof ReactionTypeEmoji) {
                joiner.add(":emoji=" + ((ReactionTypeEmoji) reaction).getEmoji());
            } else if (reaction instanceof ReactionTypeCustomEmoji) {
                joiner.add(":custom=" + ((ReactionTypeCustomEmoji) reaction).getCustomEmoji());
            } else {
                joiner.add(reaction.toString());
            }
        }
        return joiner.toString();
    }

    private String formatReactionCounts(List<ReactionCount> reactions) {
        if (reactions == null || reactions.isEmpty()) {
            return "[]";
        }
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (ReactionCount reaction : reactions) {
            if (reaction == null) {
                continue;
            }
            joiner.add(formatReactionType(reaction.getType()) + "=" + valueOrUnknown(reaction.getTotalCount()));
        }
        return joiner.toString();
    }

    private String formatReactionType(ReactionType reaction) {
        if (reaction == null) {
            return "unknown";
        }
        if (reaction instanceof ReactionTypeEmoji) {
            return ":emoji=" + ((ReactionTypeEmoji) reaction).getEmoji();
        }
        if (reaction instanceof ReactionTypeCustomEmoji) {
            return ":custom=" + ((ReactionTypeCustomEmoji) reaction).getCustomEmoji();
        }
        return reaction.toString();
    }

    private String formatBoostSource(ChatBoostSource source) {
        if (source == null) {
            return "unknown";
        }
        return source.getClass().getSimpleName();
    }

    private String formatChat(Chat chat) {
        if (chat == null) {
            return "unknown chat";
        }
        String title = firstNonNull(chat.getTitle(), chat.getUserName(), chat.getFirstName());
        if (title == null && chat.getLastName() != null) {
            title = chat.getLastName();
        }
        if (title == null) {
            title = "chat";
        }
        String idPart = chat.getId() == null ? "?" : chat.getId().toString();
        String typePart = chat.getType() == null ? "unknown" : chat.getType();
        return title + "(id=" + idPart + ",type=" + typePart + ")";
    }

    private String formatUser(User user) {
        if (user == null) {
            return "unknown user";
        }
        String display = firstNonNull(
                user.getUserName() != null ? "@" + user.getUserName() : null,
                user.getFirstName(),
                user.getLastName());
        if (display == null) {
            display = "user";
        }
        return display + "(id=" + user.getId() + ")";
    }

    private String formatCoordinate(Double latitude, Double longitude) {
        return "(" + valueOrUnknown(latitude) + "," + valueOrUnknown(longitude) + ")";
    }

    private String trimmed(String text) {
        if (text == null) {
            return "";
        }
        return text.length() > 80 ? text.substring(0, 77) + "..." : text;
    }

    private String valueOrUnknown(Object value) {
        return value == null ? "unknown" : Objects.toString(value);
    }

    private String detectMessagePayload(Message message) {
        if (message.hasPhoto()) {
            return "photo";
        }
        if (message.hasDocument()) {
            return "document";
        }
        if (message.hasVideo()) {
            return "video";
        }
        if (message.hasAnimation()) {
            return "animation";
        }
        if (message.hasSticker()) {
            return "sticker";
        }
        if (message.hasAudio()) {
            return "audio";
        }
        if (message.hasVoice()) {
            return "voice";
        }
        if (message.hasVideoNote()) {
            return "video_note";
        }
        if (message.hasPoll()) {
            return "poll";
        }
        if (message.hasLocation()) {
            return "location";
        }
        if (message.hasContact()) {
            return "contact";
        }
        if (message.hasDice()) {
            return "dice";
        }
        if (message.getGame() != null) {
            return "game";
        }
        return "unknown";
    }

    private String firstNonNull(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
