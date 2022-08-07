package de.SparkArmy.timedOperations;

import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.PunishmentType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TemporaryPunishment {

    private final User offender;
    private final PunishmentType punishment;
    private final OffsetDateTime expirationTime;
    private final Guild guild;

    public TemporaryPunishment(User offender, PunishmentType punishment, OffsetDateTime expirationTime,Guild guild) {
        this.offender = offender;
        this.punishment = punishment;
        this.expirationTime = expirationTime;
        this.guild = guild;
        writeTimedPunishmentInPunishmentInFile();
    }

    private void writeTimedPunishmentInPunishmentInFile(){
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/timed-punishments");
        if (directory == null){
            MainUtil.logger.info("Can't create/ get a directory for timed-punishments");
            return;
        }

        File file = FileHandler.getFileInDirectory(directory,"entrys.json");
        JSONObject entrys;
        if (!file.exists()){
            FileHandler.createFile(directory.getAbsolutePath(),"entrys.json");
            entrys = new JSONObject();
        }else {
            entrys = new JSONObject(Objects.requireNonNull(FileHandler.getFileContent(file)));
        }
        JSONObject entry = new JSONObject(){{
            put("user",offender.getId());
            put("type",punishment.getName());
            put("guild",guild.getId());
            put("expirationTime",expirationTime.format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss")));
        }};

        entrys.put(offender.getId() + ";" + punishment,entry);
        FileHandler.writeValuesInFile(file,entrys);
    }

    protected static @Nullable JSONObject getTimedPunishmentsFromPunishmentFile(){
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/timed-punishments");
        if (directory == null){
            MainUtil.logger.info("Can't create/ get a directory for timed-punishments");
            return null;
        }
        return new JSONObject(Objects.requireNonNull(FileHandler.getFileContent(directory, "entrys.json")));
    }
}
