package qowyn.ark.tools;

import static qowyn.ark.tools.JsonValidator.expect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import qowyn.ark.tools.data.ArkItem;
import qowyn.ark.types.ArkName;

public class ModificationFile {

  public final Map<String, String> remapDinoClassName = new HashMap<>();

  public final Map<ArkName, ArkName> remapItemArchetype = new HashMap<>();

  public final Set<ArkName> removeItems = new HashSet<>();

  public final List<ArkItem> addItems = new ArrayList<>();

  public final Map<ArkName, List<ArkItem>> replaceDefaultInventoriesMap = new HashMap<>();

  public final Map<ArkName, List<ArkItem>> replaceInventoriesMap = new HashMap<>();

  public final Map<ArkName, List<ArkItem>> addDefaultInventoriesMap = new HashMap<>();

  public final Map<ArkName, List<ArkItem>> addInventoriesMap = new HashMap<>();

  public void readJson(JsonObject object) {
    JsonValue dinoClassNamesValue = object.get("dinoClassNames");

    if (expect(dinoClassNamesValue, "dinoClassNames", JsonValue.ValueType.OBJECT)) {
      JsonObject dinoClassNames = (JsonObject) dinoClassNamesValue;

      dinoClassNames.forEach((name, value) -> {
        if (expect(value, name, JsonValue.ValueType.STRING)) {
          remapDinoClassName.put(name, ((JsonString) value).getString());
        }
      });
    }

    JsonValue itemArchetypesValue = object.get("itemArchetype");

    if (expect(itemArchetypesValue, "itemArchetype", JsonValue.ValueType.OBJECT)) {
      JsonObject itemArchetypes = (JsonObject) itemArchetypesValue;

      itemArchetypes.forEach((name, value) -> {
        if (expect(value, name, JsonValue.ValueType.STRING)) {
          remapItemArchetype.put(new ArkName(name), new ArkName(((JsonString) value).getString()));
        }
      });
    }

    JsonValue removeItemsValue = object.get("removeItems");

    if (expect(removeItemsValue, "removeItems", JsonValue.ValueType.ARRAY)) {
      for (JsonString itemClass : ((JsonArray) removeItemsValue).getValuesAs(JsonString.class)) {
        removeItems.add(new ArkName(itemClass.getString()));
      }
    }

    JsonValue addItemsValue = object.get("addItems");

    if (expect(addItemsValue, "addItems", JsonValue.ValueType.ARRAY)) {
      JsonArray itemArray = (JsonArray) addItemsValue;
      for (JsonObject item : itemArray.getValuesAs(JsonObject.class)) {
        addItems.add(new ArkItem(item));
      }
    }

    BiConsumer<String, Map<ArkName, List<ArkItem>>> inventoryLoader = (fieldName, map) -> {
      JsonValue inventoriesValue = object.get(fieldName);

      if (expect(inventoriesValue, fieldName, JsonValue.ValueType.OBJECT)) {
        JsonObject inventories = (JsonObject) inventoriesValue;

        inventories.forEach((name, value) -> {
          if (expect(value, name, JsonValue.ValueType.ARRAY)) {
            JsonArray itemArray = (JsonArray) value;
            List<ArkItem> items = new ArrayList<>();

            for (JsonObject item : itemArray.getValuesAs(JsonObject.class)) {
              items.add(new ArkItem(item));
            }

            map.put(new ArkName(name), items);
          }
        });
      }
    };

    //inventoryLoader.accept("replaceDefaultInventoryItems", replaceDefaultInventoriesMap);
    //inventoryLoader.accept("replaceInventoryItems", replaceInventoriesMap);
    inventoryLoader.accept("addToDefaultInventory", addDefaultInventoriesMap);
    inventoryLoader.accept("addToInventory", addInventoriesMap);
  }

}
