package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

//region anchors
const val DESTINATION_NAME = "[DESTINATION_NAME]"
const val BASE_ROUTE = "[ROUTE_ID]"
const val USER_COMPOSABLE_DESTINATION = "[USER_COMPOSABLE_DESTINATION]"
const val COMPOSED_ROUTE = "[COMPOSED_ROUTE]"
const val NAV_ARGUMENTS = "[NAV_ARGUMENTS]"
const val DEEP_LINKS = "[DEEP_LINKS]"
const val DESTINATION_STYLE = "[DESTINATION_STYLE]"
const val ARGS_TO_DIRECTION_METHOD = "[ARGS_TO_DIRECTION_METHOD]"
const val ARGS_FROM_METHODS = "[ARGS_FROM_METHODS]"
const val CONTENT_FUNCTION_CODE = "[CONTENT_FUNCTION_CODE]"
const val REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER = "[REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER]"
const val DESTINATION_VISIBILITY_PLACEHOLDER = "[DESTINATION_VISIBILITY_PLACEHOLDER]"
const val NAV_ARGS_DATA_CLASS = "[NAV_ARGS_DATA_CLASS]"
const val SUPERTYPE = "[SUPERTYPE]"
const val ACTIVITY_DESTINATION_FIELDS = "[ACTIVITY_DESTINATION_FIELDS]"
const val DESTINATION_LABEL = "[DESTINATION_LABEL]"
//endregion

val destinationTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName.destinations",
    imports = setOfImportable(
        "androidx.compose.runtime.Composable",
        "androidx.navigation.NavBackStackEntry",
        "androidx.navigation.NavHostController",
        "androidx.navigation.NavType",
        "$CORE_PACKAGE_NAME.scope.DestinationScope",
        "$CORE_PACKAGE_NAME.navigation.DestinationDependenciesContainer",
        "$CORE_PACKAGE_NAME.navigation.DependenciesContainerBuilder",
        "$CORE_PACKAGE_NAME.spec.*",
    ),
    sourceCode = """
$NAV_ARGS_DATA_CLASS/**
 * Generated from [$USER_COMPOSABLE_DESTINATION] 
 */
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}${DESTINATION_VISIBILITY_PLACEHOLDER} data object $DESTINATION_NAME : BaseRoute(), $SUPERTYPE {
    $ARGS_TO_DIRECTION_METHOD
    override val baseRoute: String = "$BASE_ROUTE"

    override val route: String = $COMPOSED_ROUTE
    $NAV_ARGUMENTS$DEEP_LINKS$DESTINATION_STYLE$DESTINATION_LABEL
$CONTENT_FUNCTION_CODE
    $ACTIVITY_DESTINATION_FIELDS$ARGS_FROM_METHODS
}
""".trimIndent()
)