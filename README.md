# MedBuddy – Medicine Reminder App

MedBuddy is an Android app for scheduling and managing medicine reminders with customizable dose times and duration.  
Built with Kotlin and Android Studio.

---

## Features

- Add unlimited medicines, each with custom name, dose count, and total duration
- For each medicine, choose dose times (with 12-hour/AM-PM picker)
- Set alarms to notify the user at the correct time for each dose
- Automatically stops reminders after specified days per medicine
- Complete local schedule: **no network needed**
  
---

## How It Works

- **Add Medicine**: Tap "+ Add Medicine", enter name, number of doses, and total days
- **Set Dose Times**: For each dose, tap the time button and select the required time with the system TimePicker (AM/PM supported)
- **Set Reminder**: Tap "Set Reminder" to schedule alarms for all doses
- **Stop Reminder**: Tap "Stop Reminder" on any medicine card to cancel its alarms early

Notifications will appear (with sound) at the appropriate dose time, even if the app is in the background.

---

## Build & Run Instructions

1. Open project in **Android Studio**
2. Make sure you have **Kotlin** support and latest SDK/tools
3. **Run on Emulator or Device**
   - Grant *notification* and *exact alarm* permissions on Android 12+ / 13+ (app will prompt you)
   - To ensure reliable alarms, **disable battery optimizations** for MedBuddy in settings
4. *(Optional)* For full alarm sound/pop-ups, also grant "Show on top" and "Do Not Disturb" override

---

## Troubleshooting

If you are not seeing notifications or alarm sounds:
- Check *Permissions*: Notification, Alarm, Battery Optimization
- Use *setExactAndAllowWhileIdle* for best timing, but note Android can still batch alarms in Doze
- Some device models need the app to be white-listed in battery/background settings
- Reinstall app if notification sounds don't update after changes to notification channel

---

## Project Structure

- `MainActivity.kt` — core logic for scheduling/canceling reminders, UI construction
- `AlarmReceiver.kt` — handles alarm event and fires system notification
- `activity_main.xml` — dynamic UI shell
- `res/drawable/`, `res/values/colors.xml` — metallic/gradient background resources
- AndroidManifest — AlarmReceiver registered, permissions declared

---

This project is open source for student/educational use.  
Feel free to modify and improve as needed!

---

**Built with ❤️ by Deep Das**
