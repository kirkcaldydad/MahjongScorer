package house.mcintosh.mahjong.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class JsonUtil
{
	static private ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static private ObjectMapper PRETTY_OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	static public ObjectNode createObjectNode()
	{
		return OBJECT_MAPPER.getNodeFactory().objectNode();
	}

	static public ArrayNode createArrayNode()
	{
		return OBJECT_MAPPER.getNodeFactory().arrayNode();
	}

	static public void writeFile(JsonNode node, File file) throws IOException
	{
		PRETTY_OBJECT_MAPPER.writeValue(file, node);
	}

	static public JsonNode load(File file) throws IOException
	{
		return OBJECT_MAPPER.readTree(file);
	}

	static public JsonNode load(InputStream inStream) throws IOException
	{
		return OBJECT_MAPPER.readTree(inStream);
	}

	static public String toString(JsonNode node) throws JsonProcessingException
	{
		return PRETTY_OBJECT_MAPPER.writeValueAsString(node);
	}
	
	static public <E extends Enum> E toEnumOrNull(Class<E> enumClass, JsonNode valueNode)
	{
		if (valueNode == null || valueNode.isTextual())
			return null;
		
		String value = valueNode.textValue();
		
		E[] enumValues = enumClass.getEnumConstants();
		
		for (E enumValue : enumValues)
		{
			if (enumValue.name().equals(value))
				return enumValue;
		}
		
		return null;
	}
}
