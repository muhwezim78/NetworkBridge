package com.muhwezi.networkbridge.data.model

import com.google.gson.annotations.SerializedName

data class Template(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("template_type") val templateType: String, // login, payment
    @SerializedName("html_content") val htmlContent: String,
    @SerializedName("css_content") val cssContent: String,
    @SerializedName("variables_schema") val variablesSchema: Map<String, TemplateVariableSchema>?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("user_id") val userId: String? = null // For admin view
)

data class TemplateVariableSchema(
    @SerializedName("type") val type: String, // string, color, image
    @SerializedName("default") val default: String
)

data class CreateTemplateRequest(
    @SerializedName("name") val name: String,
    @SerializedName("template_type") val templateType: String,
    @SerializedName("html_content") val htmlContent: String,
    @SerializedName("css_content") val cssContent: String,
    @SerializedName("variables_schema") val variablesSchema: Map<String, TemplateVariableSchema>,
    @SerializedName("is_active") val isActive: Boolean
)

data class RouterTemplateConfig(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("router_id") val routerId: String,
    @SerializedName("login_template_id") val loginTemplateId: String?,
    @SerializedName("payment_template_id") val paymentTemplateId: String?,
    @SerializedName("variables_values") val variablesValues: Map<String, String>?,
    @SerializedName("mobile_money_enabled") val mobileMoneyEnabled: Boolean
)

data class UpdateRouterTemplateRequest(
    @SerializedName("login_template_id") val loginTemplateId: String? = null,
    @SerializedName("payment_template_id") val paymentTemplateId: String? = null,
    @SerializedName("variables_values") val variablesValues: Map<String, String>? = null,
    @SerializedName("mobile_money_enabled") val mobileMoneyEnabled: Boolean? = null
)

data class FileUploadResponse(
    @SerializedName("url") val url: String,
    @SerializedName("path") val path: String
)
