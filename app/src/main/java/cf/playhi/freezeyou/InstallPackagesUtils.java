package cf.playhi.freezeyou;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

final class InstallPackagesUtils {

    static void notifyFinishNotification(
            Context context, NotificationManager notificationManager,
            Notification.Builder builder, boolean install,
            String beOperatedPackageName,
            String title, String text, boolean success) {

        if (beOperatedPackageName == null)
            return;// null 无法取得通知特征 hashcode

        // 小图标
        builder.setSmallIcon(R.drawable.ic_notification);

        builder.setProgress(0, 0, false);

        if (install) {

            // 提示标题
            if (title != null)
                builder.setContentTitle(title);

            // 提示文本
            if (text != null)
                builder.setContentText(text);

            if (success) {

                // 大图标
                builder.setLargeIcon(
                        ApplicationIconUtils.getBitmapFromDrawable(
                                ApplicationIconUtils.getApplicationIcon(
                                        context,
                                        beOperatedPackageName,
                                        ApplicationInfoUtils
                                                .getApplicationInfoFromPkgName(
                                                        beOperatedPackageName,
                                                        context),
                                        false)
                        )
                );

                // 点击打开
                Intent resultIntent =
                        context.getPackageManager().getLaunchIntentForPackage(beOperatedPackageName);
                if (resultIntent != null) {
                    PendingIntent resultPendingIntent =
                            PendingIntent
                                    .getActivity(
                                            context,
                                            (beOperatedPackageName + "@InstallPackagesNotification").hashCode(),
                                            resultIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(resultPendingIntent);
                    builder.setAutoCancel(true);

                    if (text == null)
                        builder.setContentText(context.getString(R.string.openImmediately));

                }
            } else {
                // 错误信息弹窗
                PendingIntent resultPendingIntent =
                        PendingIntent
                                .getActivity(
                                        context,
                                        (beOperatedPackageName + "@InstallPackagesNotification").hashCode(),
                                        new Intent(context, ShowSimpleDialogActivity.class)
                                                .putExtra("title", title)
                                                .putExtra("text", text),
                                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);
            }

            notificationManager.notify(
                    (beOperatedPackageName + "@InstallPackagesNotification").hashCode(), builder.getNotification()
            );

        } else {

            if (text != null)
                builder.setContentText(text);

            if (title != null)
                builder.setContentTitle(title);

            PendingIntent resultPendingIntent =
                    PendingIntent
                            .getActivity(
                                    context,
                                    (beOperatedPackageName + "@InstallPackagesNotification").hashCode(),
                                    new Intent(context, ShowSimpleDialogActivity.class)
                                            .putExtra("title", title)
                                            .putExtra("text", text),
                                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            notificationManager.notify(
                    (beOperatedPackageName + "@InstallPackagesNotification").hashCode(), builder.getNotification()
            );

        }
    }

    /**
     * @param context     Context
     * @param apkFilePath 文件路径
     * @param noCheck     不检查是否为安装过程生成的，直接删除
     */
    static void deleteTempFile(Context context, String apkFilePath, boolean noCheck) {
        if (noCheck || apkFilePath.startsWith(context.getExternalCacheDir() + File.separator + "ZDF-")) {
            File file = new File(apkFilePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    static void copyFile(InputStream in, String apkFilePath) throws IOException {
        if (in == null) {
            throw new IOException("InputStream is null");
        }
        if (Build.VERSION.SDK_INT < 26) {
            FileOutputStream out = new FileOutputStream(apkFilePath);
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            in.close();
        } else {
            Files.copy(in, new File(apkFilePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
