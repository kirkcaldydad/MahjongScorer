package house.mcintosh.mahjong.migration;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import house.mcintosh.mahjong.util.TimeUtil;

/**
 * Handles migration of Game (or any other) files following an upgrade.
 */
class Migrator
{
	/**
	 * All the migrations, ordered in the sequence that they should run. New migrations should
	 * be added to the end of the list.
	 */


	public void runMigrations(Context context) throws IOException
	{
		// Run a method to do each migration, unless the migration is marked as having run before.
		
		MigrationStatus migrationStatus = new MigrationStatus(context);
		
		for (MigrationType migrationType : MigrationType.values())
		{
			if (migrationStatus.shouldRun(migrationType))
				runMigration(migrationType, migrationStatus, context);
		}
	}
	
	private boolean runMigration(MigrationType type, MigrationStatus status, Context context) throws IOException
	{
		Class<? extends Migration> migrationClass = type.getMigrationClass();
		String startTime = TimeUtil.getUTCNow();
		String endTime;
		
		try
		{
			JsonNode migrationLog = migrationClass.getConstructor().newInstance().migrate(context);
			endTime = TimeUtil.getUTCNow();
			
			if (migrationLog == null)
			{
				status.recordFailedMigration(type, startTime, endTime, null);
				return false;
			}
			
			status.recordSuccessfulMigration(type, startTime, endTime, migrationLog);
			return true;
			
		}
		catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | IOException e)
		{
			endTime = TimeUtil.getUTCNow();
			status.recordFailedMigration(type, startTime, endTime, e);
			return false;
		}
	}
}
