package house.mcintosh.mahjong.migration;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import house.mcintosh.mahjong.exception.MahjongException;
import house.mcintosh.mahjong.model.GameSummary;
import house.mcintosh.mahjong.util.JsonUtil;

class MoveGamesToInternalStorageMigrator implements Migration
{
	private final static String LOG_TAG = MoveGamesToInternalStorageMigrator.class.getName();

	private static final FilenameFilter GAME_FILENAME_FILTER =
		new FilenameFilter()
		{
			private final Pattern FILE_PATTERN = Pattern.compile("^game[0-9]{17}\\.json$");

			@Override
			public boolean accept(File dir, String name)
			{
				Matcher matcher = FILE_PATTERN.matcher(name);
				return matcher.matches();
			}
		};

	@Override
	public JsonNode migrate(Context context) throws MahjongException
	{
		try
		{
			List<GameSummary> allGames = new ArrayList<>();

			File externalDirectory = context.getExternalFilesDir(null);

			if (!externalDirectory.isDirectory())
			{
				Log.e(LOG_TAG, "Cannot load game files - no directory.");
				return null;		// return null, indicating that it is worth trying the migration again - in case external storage reappears.
			}

			JsonNode migrationLog = JsonUtil.createObjectNode();
			ArrayNode logEntries = (ArrayNode)migrationLog.withArray("migratedFiles");

			File[] externalFiles = externalDirectory.listFiles(GAME_FILENAME_FILTER);

			if (externalFiles.length == 0)
				return migrationLog;    // Did nothing. There was nothing to do.

			File internalDirectory = context.getFilesDir();
			File internalGamesDirectory = new File(internalDirectory, "games");

			internalGamesDirectory.mkdir();

			for (File externalGameFile : externalFiles)
			{
				File internalFile = new File(internalGamesDirectory, externalGameFile.getName());

				Files.move(externalGameFile.toPath(), internalFile.toPath());

				logEntries.add(
					JsonUtil.createObjectNode()
						.put("from", externalGameFile.getAbsolutePath())
						.put("to", internalFile.getAbsolutePath()));

				Log.i(LOG_TAG, String.format("Moved %s to %s", externalGameFile.getAbsolutePath(), internalFile.getAbsolutePath()));
			}

			return migrationLog;
		}
		catch(IOException ioe)
		{
			throw new MahjongException("Failed to move game file", ioe);
		}
	}
}
