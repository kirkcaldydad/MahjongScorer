package house.mcintosh.mahjong.migration;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

import house.mcintosh.mahjong.util.JsonUtil;
import house.mcintosh.mahjong.util.TimeUtil;

/**
 * Manages a json file that records the status of migrations.
 * Each migration is recorded as an object in a migrations array, recording information about
 * the migration and when it was run.
 */

class MigrationStatus
{
	private final static String LOG_TAG = MigrationStatus.class.getName();

	private final static String CURRENT_VERSION = "1";
	
	private enum Status
	{
		COMPLETED,
		ATTEMPTED;
		
		static Status fromName(JsonNode name)
		{
			return JsonUtil.toEnumOrNull(Status.class, name);
		}
	}

	private final File m_file;
	private ObjectNode m_status;

	public MigrationStatus(Context context) throws IOException
	{
		m_file = new File(context.getFilesDir(), "migrationStatus.json");

		ensureFileExists();

		m_status = (ObjectNode) JsonUtil.load(m_file);
	}

	private void ensureFileExists()
	{
		if (m_file.exists())
			return;

		ObjectNode fileContent = JsonUtil.createObjectNode();

		String now = TimeUtil.getUTCNow();

		fileContent.put("version", CURRENT_VERSION);
		fileContent.put("createdOn", now);
		fileContent.put("lastModifiedOn", now);
		fileContent.withArray("migrations");

		try
		{
			JsonUtil.writeFile(fileContent, m_file);
		}
		catch (IOException ioe)
		{
			Log.e(LOG_TAG, "Cannot write file: " + m_file.getAbsolutePath() + " " + ioe.getMessage());

			// TODO: display error message.
			return;
		}
	}
	
	/**
	 * Determine whether or not the given migration needs to be run.
	 */
	public boolean shouldRun(MigrationType migrationType)
	{
		ObjectNode migrationStatus = getStatusForMigration(migrationType);
		
		if (migrationStatus == null)
			return true;
		
		return Status.fromName(migrationStatus.get("status")) != Status.COMPLETED;
	}
	
	private ObjectNode getStatusForMigration(MigrationType migrationType)
	{
		for (JsonNode migrationStatus : m_status.withArray("migrations"))
		{
			MigrationType recordedMigrationType = MigrationType.fromName(migrationStatus.get("type"));
			
			if (recordedMigrationType == migrationType)
				return (ObjectNode) migrationStatus;
		}
		
		return null;
	}
	
	public void recordSuccessfulMigration(MigrationType type, String startTime, String endTime, JsonNode migrationLog) throws IOException
	{
		ObjectNode migrationStatus = getStatusForMigration(type);
		
		migrationStatus.put("status", Status.COMPLETED.name());
		migrationStatus.put("startedAt", startTime);
		migrationStatus.put("endedAt", endTime);
		migrationStatus.set("log", migrationLog);
		migrationStatus.remove("cause");
		
		save();
	}
	
	public void recordFailedMigration(MigrationType type, String startTime, String endTime, Exception cause) throws IOException
	{
		ObjectNode migrationStatus = getStatusForMigration(type);
		
		migrationStatus.put("status", Status.ATTEMPTED.name());
		migrationStatus.put("startedAt", startTime);
		migrationStatus.put("endedAt", endTime);
		migrationStatus.remove("log");
		migrationStatus.put("cause", cause.getMessage());
		
		save();
	}
	
	private void save() throws IOException
	{
		m_status.put("lastModifiedOn", TimeUtil.getUTCNow());
		
		JsonUtil.writeFile(m_status, m_file);
	}
}
