<div align="center">

# SolinOne · سوپراپ همه کاره شخصی

**مدیریت مالی شخصی، تقویم هوشمند شمسی، امنیت بیومتریک و یادآور یکپارچه**

<p>
<img src="https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white">
<img src="https://img.shields.io/badge/Kotlin-2.1-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">
<img src="https://img.shields.io/badge/Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white">
<img src="https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge">
</p>

</div>

<div dir="rtl" style="font-family: 'Vazirmatn', sans-serif;">

## 🌟 درباره برنامه SolinOne

برنامه **SolinOne** یک سوپراپ شخصی، کاملاً آفلاین و امن برای اندروید است که امکانات پیشرفته برنامه‌های معتبر را در یک محیط یکپارچه، مدرن و با زبان طراحی **Material 3** ترکیب می‌کند:

1. **مدیریت مالی شخصی (بر پایه آرا - Ara)**:
   - تشخیص هوشمند پیامک‌های بانکی بدون وابستگی به سرور و بدون ارسال داده به بیرون
   - صف بازبینی امن برای تراکنش‌ها
   - مدیریت بودجه، سقف هزینه‌ها و تعهدات مالی

2. **تقویم هوشمند شمسی (بر پایه Persian Calendar)**:
   - موتور محاسباتی دقیق تقویم شمسی و میلادی
   - رویدادها، مناسبت‌های رسمی و تعطیلات
   - نمای اختصاصی ماهانه و روزانه

3. **داشبورد یکپارچه («امروز من»)**:
   - خلاصه وضعیت تقویم امروز، مناسبت‌ها و سررسیدهای مالی همگی در یک کارت واحد

4. **امنیت و حریم خصوصی**:
   - قفل بیومتریک (اثر انگشت و تشخیص چهره)
   - قابلیت مات کردن صفحه برنامه در بخش برنامه‌های اخیر (Recent Apps) برای حفظ محرمانگی مالی

---

## 🏗️ ساختار ماژولار

```
SolinOne/
├── app/                  # ماژول اصلی برنامه، ناوبری، صفحه داشبورد و تنظیمات
├── core/
│   ├── ui/               # سیستم طراحی Material 3، فونت‌های وزیرمتن و پالت رنگی
│   ├── calendar/         # موتور محاسباتی مشترک تقویم شمسی (JDN)
│   └── security/         # سیستم امنیت بیومتریک و قفل نرم‌افزار
├── features/
│   ├── finance/          # ماژول مدیریت مالی و موتور تشخیص پیامک بانکی
│   └── calendar/         # ماژول تقویم شمسی و رویدادها
└── .github/workflows/    # پایپ‌لاین بیلد و انتشار خودکار APK با گیت‌هاب اکشن
```

---

## 🚀 ساخت پروژه (Build)

برای ساخت نسخه دیباگ:
```bash
./gradlew assembleDebug
```

برای اجرای تست‌های واحد موتور تقویم و مالی:
```bash
./gradlew test
```

## 📜 لایسنس

این پروژه تحت لایسنس عمومی **GNU General Public License v3.0 (GPL-3.0)** منتشر شده است.

</div>
