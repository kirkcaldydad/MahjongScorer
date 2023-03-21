package house.mcintosh.mahjong.migration;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;

import house.mcintosh.mahjong.exception.MahjongException;

/**
 * A migration that updates games or other data files.
 */

interface Migration
{
	/**
	 * Run the migration, returning a json object that is expected to contain a log of the
	 * migration. A return of null indicates that the migration did nothing and can be tried again.
	 * An exception indicates that the migration failed.
	 */
	JsonNode migrate(Context context) throws MahjongException;
}
