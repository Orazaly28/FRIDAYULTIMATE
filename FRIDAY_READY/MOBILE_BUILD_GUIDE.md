# 📱 F.R.I.D.A.Y. — Сборка на телефоне

## Метод 1: AIDE (рекомендуется)

### Шаг 1: Установка AIDE
1. Google Play → "AIDE - Android Java IDE"
2. Установить приложение (45 МБ)
3. Открыть AIDE

### Шаг 2: Открытие проекта
1. В AIDE нажать "Open Project"
2. Выбрать папку FRIDAY_MOBILE
3. Подождать индексацию (1-2 мин)

### Шаг 3: Сборка APK
1. Меню → Run (▶) или F5
2. Дождаться компиляции (5-10 мин)
3. Если ошибки → скриншот и отправить Боссу

### Шаг 4: Установка
1. APK появится в папке проекта: app/build/outputs/apk/debug/
2. Нажать на файл → Установить
3. Разрешить установку из неизвестных источников

---

## Метод 2: Termux (продвинутый)

### Установка Termux
1. F-Droid → Termux (НЕ из Google Play!)
2. Или скачать: https://f-droid.org/en/packages/com.termux/

### Команды в Termux:
```bash
# Обновление пакетов
pkg update && pkg upgrade -y

# Установка инструментов
pkg install -y openjdk-17 gradle wget unzip

# Переход в папку проекта
cd /storage/emulated/0/Download/FRIDAY_MOBILE

# Сборка APK
gradle assembleDebug

# APK будет здесь:
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

---

## Метод 3: Онлайн-IDE (самый простой)

### Replit Mobile:
1. Открыть браузер → https://replit.com
2. Зарегистрироваться (бесплатно)
3. Create Repl → Import from GitHub
4. Вставить URL проекта
5. Run → APK соберётся автоматически

---

## Возможные ошибки

### "Out of memory"
- Закрыть другие приложения
- Очистить кэш AIDE
- Перезагрузить телефон

### "SDK not found"
- AIDE → Settings → SDK Manager → Download SDK 34

### "Gradle sync failed"
- Проверить интернет
- Удалить .gradle в папке проекта
- Повторить сборку

---

## Контакты
Любые проблемы → скриншот ошибки → отправить Боссу
