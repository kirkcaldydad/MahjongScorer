package house.mcintosh.mahjong.migration;

import com.fasterxml.jackson.databind.JsonNode;

import house.mcintosh.mahjong.util.JsonUtil;

/**
 * All the migrations, ordered in the sequence that they should run. New migrations should
 * be added to the end of the list.
 */

enum MigrationType
{
	MoveGamesToInternalStorage(MoveGamesToInternalStorageMigrator.class);
	
	private Class<? extends Migration> s_migrationClass;
	
	MigrationType(Class<? extends Migration> migration)
	{
		s_migrationClass = migration;
	}
	
	static MigrationType fromName(JsonNode name)
	{
		return JsonUtil.toEnumOrNull(MigrationType.class, name);
	}
	
	Class<? extends Migration> getMigrationClass()
	{
		return s_migrationClass;
	}
}
