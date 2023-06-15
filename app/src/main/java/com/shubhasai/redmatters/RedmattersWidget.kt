package com.shubhasai.redmatters

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class RedmattersWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        val widgetViews = RemoteViews(context.packageName, R.layout.redmatters_widget)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
            val intent = Intent(context, SendemergencyActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            // Set the click listener on the widget
            widgetViews.setOnClickPendingIntent(R.id.btn_sendalert, pendingIntent)
//            val intent2 = Intent(context, BloodrequestActivity::class.java)
//            val pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_IMMUTABLE)
//
//            // Set the click listener on the widget
//            widgetViews.setOnClickPendingIntent(R.id.btn_sendblood, pendingIntent2)

            // Update the widget views
            appWidgetManager.updateAppWidget(appWidgetId, widgetViews)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.redmatters_widget)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}