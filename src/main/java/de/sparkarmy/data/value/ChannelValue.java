package de.sparkarmy.data.value;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public enum ChannelValue implements IValue {
    FEEDBACK_CHANNEL(1, false, false, "feedback-channel"),
    ARCHIVE_CATEGORY(2, false, false, "archive-category"),
    MESSAGE_LOG(10, true, false, "message-log"),
    MEMBER_LOG(11, true, false, "member-log"),
    COMMAND_LOG(12, true, false, "command-log"),
    SERVER_LOG(13, true, false, "server-log"),
    VOICE_LOG(14, true, false, "voice-log"),
    MOD_LOG(15, true, false, "mod-log"),
    LEAVE_LOG(16, true, false, "leave-log"),
    ALLOW_TEXT(30, false, true, "allow-text"),
    ALLOW_LINK(31, false, true, "allow-link"),
    ALLOW_FILE(32, false, true, "allow-file"),
    ALLOW_ATTACHMENT(33, false, true, "allow-attachment"),

    UNKNOWN(-1, false, false, "unknown permission");

    private final long offset;
    private final long raw;
    private final boolean isLogChannel;
    private final boolean isPermission;
    private final String name;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelValue.class);

    ChannelValue(long offset, boolean isLogChannel, boolean isPermission, String name) {
        this.offset = offset;
        this.raw = 1L << offset;
        this.isLogChannel = isLogChannel;
        this.isPermission = isPermission;
        this.name = name;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public long getValueRaw() {
        return raw;
    }

    public boolean isLogChannel() {
        return isLogChannel;
    }

    public boolean isPermission() {
        return isPermission;
    }

    public String getName() {
        return name;
    }

    @Contract(mutates = "this")
    public static long getRaw(ChannelValue @NotNull ... values) {
        long raw = 0;
        for (ChannelValue value : values) {
            if (value != null && value != UNKNOWN) {
                raw |= value.raw;
            }
        }
        return raw;
    }

    public static long getAllValuesRaw() {
        return getRaw(ChannelValue.values());
    }

    public static ChannelValue getByName(String name) {
        return Arrays.stream(ChannelValue.values()).filter(x -> x.getName().equals(name)).findFirst().orElse(UNKNOWN);
    }
}
