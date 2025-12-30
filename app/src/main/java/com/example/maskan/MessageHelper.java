package com.example.maskan;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;

public class MessageHelper {
    private Context applicationContext;
    private static MessageHelper instance;

    // أنواع الرسائل
    public static final int TYPE_SUCCESS = 1;
    public static final int TYPE_ERROR = 2;
    public static final int TYPE_WARNING = 3;
    public static final int TYPE_INFO = 4;
    public static final int TYPE_SAVE = 5;
    public static final int TYPE_DELETE = 6;
    public static final int TYPE_LOADING = 7;

    // للتحكم في عدد الـ Toasts النشطة
    private static final int MAX_ACTIVE_TOASTS = 3;
    private static int activeToastCount = 0;
    private static Toast currentToast;

    // للتحكم في الـ Dialogs النشطة
    private Dialog currentDialog;
    private CountDownTimer currentTimer;
    private Dialog loadingDialog;

    // تهيئة Singleton - يجب استدعاؤها مرة واحدة في Application
    public static void init(Context context) {
        if (instance == null) {
            instance = new MessageHelper(context);
        }
    }

    public static MessageHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MessageHelper must be initialized first. Call MessageHelper.init(context) in your Application class.");
        }
        return instance;
    }

    private MessageHelper(Context context) {
        this.applicationContext = context.getApplicationContext();
    }

    // ==================== 📱 الطريقة الرئيسية ====================

    /**
     * عرض رسالة dialog
     */
    public void showDialog(String title, String message, int type, boolean autoClose) {
        showCustomDialog(title, message, type, autoClose, null);
    }

    /**
     * عرض رسالة dialog مع زر إجراء
     */
    public void showDialog(String title, String message, int type,
                           String buttonText, View.OnClickListener buttonClickListener) {
        showCustomDialog(title, message, type, false, buttonText, buttonClickListener);
    }

    // ==================== 🎨 الدالة الأساسية ====================

    private void showCustomDialog(String title, String message, int type,
                                  boolean autoClose, String buttonText,
                                  View.OnClickListener buttonClickListener) {
        // إغلاق أي نافذة سابقة مفتوحة
        closeCurrentDialog();

        // إنشاء الـ Dialog
        final Dialog dialog = new Dialog(applicationContext, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_message_dialog);

        // إعدادات الـ Window
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setWindowAnimations(R.style.DialogAnimation);
        }

        // الحصول على العناصر
        CardView cardDialog = dialog.findViewById(R.id.cardDialog);
        LottieAnimationView lottieAnimation = dialog.findViewById(R.id.lottieAnimation);
        ImageView ivIcon = dialog.findViewById(R.id.ivIcon);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        Button btnAction = dialog.findViewById(R.id.btnAction);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // إخفاء العناصر غير المستخدمة
        if (lottieAnimation != null) {
            lottieAnimation.setVisibility(View.GONE);
        }
        if (ivIcon != null) {
            ivIcon.setVisibility(View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(autoClose ? View.VISIBLE : View.GONE);
        }
        if (btnCancel != null) {
            btnCancel.setVisibility(buttonClickListener != null ? View.VISIBLE : View.GONE);
        }

        // تعيين البيانات حسب النوع
        setupDialogByType(type, title, message, lottieAnimation, ivIcon, cardDialog);

        // تعيين النصوص
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        if (tvMessage != null) {
            tvMessage.setText(message);
        }

        if (btnAction != null) {
            if (buttonText != null) {
                btnAction.setText(buttonText);
            } else {
                btnAction.setText("حسناً");
            }
        }

        // إعداد الزر الرئيسي
        if (btnAction != null) {
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateButtonClick(v, new Runnable() {
                        @Override
                        public void run() {
                            if (buttonClickListener != null) {
                                buttonClickListener.onClick(v);
                            }
                            dismissDialogWithAnimation(dialog);
                        }
                    });
                }
            });
        }

        // إعداد زر الإلغاء
        if (btnCancel != null) {
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateButtonClick(v, new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            currentDialog = null;
                        }
                    });
                }
            });
        }

        // إغلاق تلقائي
        if (autoClose) {
            startAutoCloseTimer(dialog, progressBar);
        }

        // حفظ المرجع
        currentDialog = dialog;

        // عرض الـ Dialog مع أنيميشن
        showDialogWithAnimation(dialog, cardDialog);
    }

    private void showCustomDialog(String title, String message, int type,
                                  boolean autoClose, View.OnClickListener buttonClickListener) {
        showCustomDialog(title, message, type, autoClose, null, buttonClickListener);
    }

    // ==================== ⚙️ دوال المساعدة ====================

    private void setupDialogByType(int type, String title, String message,
                                   LottieAnimationView lottieAnimation, ImageView icon, CardView card) {
        // التأكد من أن lottieAnimation غير null
        if (lottieAnimation == null) {
            return;
        }

        switch (type) {
            case TYPE_SUCCESS:
            case TYPE_SAVE:
                lottieAnimation.setVisibility(View.VISIBLE);
                lottieAnimation.setAnimation(R.raw.success_animation);
                lottieAnimation.playAnimation();
                if (card != null) {
                    card.setCardBackgroundColor(applicationContext.getColor(R.color.success_color));
                }
                break;

            case TYPE_ERROR:
                lottieAnimation.setVisibility(View.VISIBLE);
                lottieAnimation.setAnimation(R.raw.error_animation);
                lottieAnimation.playAnimation();
                if (card != null) {
                    card.setCardBackgroundColor(applicationContext.getColor(R.color.error_color));
                }
                break;

            case TYPE_WARNING:
                if (icon != null) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageResource(R.drawable.ic_warning);
                }
                if (card != null) {
                    card.setCardBackgroundColor(applicationContext.getColor(R.color.warning_color));
                }
                break;

            case TYPE_INFO:
                if (icon != null) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageResource(R.drawable.ic_info);
                }
                if (card != null) {
                    card.setCardBackgroundColor(applicationContext.getColor(R.color.info_color));
                }
                break;

            case TYPE_DELETE:
                lottieAnimation.setVisibility(View.VISIBLE);
                lottieAnimation.setAnimation(R.raw.delete_animation);
                lottieAnimation.playAnimation();
                if (card != null) {
                    card.setCardBackgroundColor(applicationContext.getColor(R.color.delete_color));
                }
                break;

            case TYPE_LOADING:
                // التعامل معها بشكل خاص
                break;
        }
    }

    private void showDialogWithAnimation(final Dialog dialog, CardView cardDialog) {
        if (cardDialog == null) return;

        cardDialog.setScaleX(0.8f);
        cardDialog.setScaleY(0.8f);
        cardDialog.setAlpha(0f);

        cardDialog.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        dialog.show();
    }

    private void dismissDialogWithAnimation(final Dialog dialog) {
        CardView cardDialog = dialog.findViewById(R.id.cardDialog);
        if (cardDialog == null) {
            dialog.dismiss();
            currentDialog = null;
            return;
        }

        cardDialog.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dialog.dismiss();
                        currentDialog = null;
                    }
                })
                .start();
    }

    private void animateButtonClick(View button, final Runnable action) {
        if (button == null) return;

        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .withEndAction(action)
                                .start();
                    }
                })
                .start();
    }

    private void startAutoCloseTimer(final Dialog dialog, ProgressBar progressBar) {
        // إلغاء أي مؤقت سابق
        if (currentTimer != null) {
            currentTimer.cancel();
        }

        currentTimer = new CountDownTimer(3000, 30) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((3000 - millisUntilFinished) / 30);
                if (progressBar != null) {
                    progressBar.setProgress(progress);
                }
            }

            @Override
            public void onFinish() {
                dismissDialogWithAnimation(dialog);
            }
        }.start();

        // إلغاء المؤقت عند إغلاق الـ Dialog
        dialog.setOnDismissListener(dialogInterface -> {
            if (currentTimer != null) {
                currentTimer.cancel();
                currentTimer = null;
            }
        });
    }

    private void closeCurrentDialog() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }
    }

    // ==================== 📝 دوال مختصرة للاستخدام السريع ====================

    public void showSuccess(String message) {
        showDialog("نجاح", message, TYPE_SUCCESS, true);
    }

    public void showError(String message) {
        showDialog("خطأ", message, TYPE_ERROR, false);
    }

    public void showWarning(String message) {
        showDialog("تحذير", message, TYPE_WARNING, true);
    }

    public void showInfo(String message) {
        showDialog("معلومة",
                message,
                TYPE_INFO,
                true);
    }

    public void showSaveSuccess() {
        showDialog("تم الحفظ", "تم حفظ البيانات بنجاح ✅", TYPE_SAVE, true);
    }

    public void showDeleteConfirm(String message, View.OnClickListener onConfirm) {
        showDialog("تأكيد الحذف", message, TYPE_DELETE,
                "نعم، احذف", onConfirm);
    }

    // ==================== 🍞 Toast Messages ====================

    public void showToast(String message, int type) {
        // التحكم في عدد الـ Toasts النشطة
        if (activeToastCount >= MAX_ACTIVE_TOASTS) {
            // إلغاء الـ Toast السابق إذا كان هناك أكثر من 3
            cancelCurrentToast();
            return;
        }

        // إلغاء الـ Toast السابق قبل إنشاء جديد
        cancelCurrentToast();

        LayoutInflater inflater = LayoutInflater.from(applicationContext);
        View layout = inflater.inflate(R.layout.custom_toast, null);

        CardView toastCard = layout.findViewById(R.id.toastCard);
        ImageView ivIcon = layout.findViewById(R.id.ivIcon);
        TextView tvMessage = layout.findViewById(R.id.tvMessage);

        tvMessage.setText(message);

        // تلوين حسب النوع
        int color = getColorByType(type);
        toastCard.setCardBackgroundColor(color);

        Toast toast = new Toast(applicationContext);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.setView(layout);

        // أنيميشن
        toastCard.setAlpha(0f);
        toastCard.setTranslationY(-100f);

        toastCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // زيادة العداد
        activeToastCount++;
        currentToast = toast;

        toast.show();

        // إغلاق تلقائي مع أنيميشن وتقليل العداد
        new Handler().postDelayed(() -> {
            toastCard.animate()
                    .alpha(0f)
                    .translationY(-100f)
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            toast.cancel();
                            activeToastCount--;
                            currentToast = null;
                        }
                    })
                    .start();
        }, 3000);
    }

    private void cancelCurrentToast() {
        if (currentToast != null) {
            currentToast.cancel();
            currentToast = null;
        }
    }

    private int getColorByType(int type) {
        switch (type) {
            case TYPE_SUCCESS: return applicationContext.getColor(R.color.success_color);
            case TYPE_ERROR: return applicationContext.getColor(R.color.error_color);
            case TYPE_WARNING: return applicationContext.getColor(R.color.warning_color);
            case TYPE_INFO: return applicationContext.getColor(R.color.info_color);
            default: return applicationContext.getColor(R.color.success_color);
        }
    }

    // ==================== ⏳ رسائل التحميل ====================

    public void showLoading(String message) {
        // إخفاء أي نافذة تحميل سابقة
        hideLoading();

        loadingDialog = new Dialog(applicationContext, R.style.DialogTheme);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.custom_loading_dialog);
        loadingDialog.setCancelable(false);

        Window window = loadingDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        LottieAnimationView lottieLoading = loadingDialog.findViewById(R.id.lottieLoading);
        TextView tvLoadingMessage = loadingDialog.findViewById(R.id.tvLoadingMessage);

        lottieLoading.setAnimation(R.raw.loading_animation);
        lottieLoading.playAnimation();
        tvLoadingMessage.setText(message);

        loadingDialog.show();
    }

    public void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        loadingDialog = null;
    }

    // ==================== 🧹 دوال التنظيف ====================

    /**
     * تنظيف جميع الموارد عند تدمير Activity
     */
    public void cleanup() {
        closeCurrentDialog();
        hideLoading();
        cancelCurrentToast();

        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }

        activeToastCount = 0;
    }
}