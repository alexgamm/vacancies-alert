package vacanciesalert.telegram.update;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

public class UpdateHandler {

    public static UpdateType handleUpdate(Update update) {
        String chatId = Optional.ofNullable(update.getMessage())
                .map(Message::getChatId)
                .map(String::valueOf)
                .orElse(null);
        if (chatId != null) {
            return UpdateType.USER_COMMAND;
        }
        if (update.hasCallbackQuery()) {
            return UpdateType.CALLBACK;
        }
        String newChatMemberStatus = Optional.ofNullable(update.getMyChatMember())
                .map(ChatMemberUpdated::getNewChatMember)
                .map(ChatMember::getStatus)
                .orElse(null);
        if ("kicked".equals(newChatMemberStatus)) {
            return UpdateType.USER_BOT_BLOCK;
        }
        return UpdateType.INVALID_COMMAND;
    }
}
