package ${configYAML.apiPackagePath}.client.permission;

import ${configYAML.apiPackagePath}.client.json.BaseJSONParser;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Generated;

/**
* @author ${configYAML.author}
* @generated
*/
@Generated("")
public class Permission {

	public String[] getActionIds() {
		return _actionIds;
	}

	public String getRoleName() {
		return _roleName;
	}

	public void setActionIds(String[] actionIds) {
		this._actionIds = actionIds;
	}

	public void setRoleName(String roleName) {
		this._roleName = roleName;
	}

	private String[] _actionIds;
	private String _roleName;

	private static class PermissionJSONParser<T> extends BaseJSONParser<Permission> {

		@Override
		protected Permission createDTO() {
			return new Permission();
		}

		@Override
		protected Permission[] createDTOArray(int size) {
			return new Permission[size];
		}

		@Override
		protected void setField(Permission permission, String jsonParserFieldName, Object jsonParserFieldValue) {
			if (Objects.equals(jsonParserFieldName, "actionIds")) {
				if (jsonParserFieldValue != null) {
					permission.setActionIds((String[])jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "roleName")) {
				if (jsonParserFieldValue != null) {
					permission.setRoleName((String)jsonParserFieldValue);
				}
			}
			else {
				throw new IllegalArgumentException("Unsupported field name " + jsonParserFieldName);
			}
		}
	}

}