# Telegram Listener

Этот проект поднимает простого Telegram Long Polling бота, который выводит в консоль и/или файл
содержимое каждого полученного update. Это удобно, когда нужно быстро проверить, какие события
Telegram API отправляет вашему боту ("webhook" payload'ы) без настройки собственного сервера.

## Возможности
- Поддержка всех основных типов Update (сообщения, инлайн-запросы, опросы, реакции, chat boost и т.д.)
- Подробный человекочитаемый вывод или NDJSON-строка на выбор
- Логирование в stdout и/или файл (append режим)
- Простая конфигурация через переменные окружения / системные свойства
- JUnit-тесты, которые покрывают форматирование выходных данных

## Требования
- JDK 17+
- Maven 3.9+
- Существующий Telegram бот и токен (`TELEGRAM_BOT_TOKEN`)
- Username бота (`TELEGRAM_BOT_USERNAME`)

## Быстрый старт
```bash
# собрать и прогнать тесты
mvn verify

# запустить бота (env vars предпочтительнее)
TELEGRAM_BOT_TOKEN=123:abc \
TELEGRAM_BOT_USERNAME=my_listener_bot \
TELEGRAM_OUTPUT_FORMAT=json \
TELEGRAM_LOG_FILE=updates.log \
mvn exec:java
```

После запуска, бот зарегистрируется через Long Polling и будет печатать каждое событие в stdout.
Если задан `TELEGRAM_LOG_FILE`, каждая строка продублируется в файл (режим append).

## Конфигурация
| Ключ | Где задаётся | Описание |
| --- | --- | --- |
| `TELEGRAM_BOT_TOKEN` | переменная окружения или `-DTELEGRAM_BOT_TOKEN` | токен, который выдаёт @BotFather |
| `TELEGRAM_BOT_USERNAME` | переменная окружения или `-DTELEGRAM_BOT_USERNAME` | username бота (без @) |
| `TELEGRAM_OUTPUT_FORMAT` | переменная окружения или `-DTELEGRAM_OUTPUT_FORMAT` | `text` (по умолчанию) или `json`/`ndjson` |
| `TELEGRAM_LOG_FILE` | переменная окружения или `-DTELEGRAM_LOG_FILE` | путь к файлу, куда будут дописываться копии update |

Если переменная/свойство не задано, приложение упадёт с `IllegalStateException` и сообщит, какое
значение отсутствует.

## Структура проекта
- `BotLauncher` — точка входа, которая читает конфиг, подбирает формат вывода и регистрирует бота
- `TelegramTriggerBot` — реализация `TelegramLongPollingBot`
- `UpdatePrinter` — формирует диагностическую строку или NDJSON и фан-аутит вывод в stdout/файл
- `UpdatePrinterTest` — гарантирует, что новые типы Update не будут пропущены

## Как читать вывод
Каждый блок начинается с `=== Update #<id> ===` и заканчивается `=============================`.
Внутри находятся секции вида `[MESSAGE]`, `[INLINE_QUERY]` и т.п.; если секции нет, значит Telegram не
прислал соответствующий payload. Это удобно для отладки inline-кнопок, оплат, реакций, chat-join
events и прочих сложных сценариев.

## Docker запуск
```bash
# собрать образ
docker build -t telegram-listener .

# передать токен/username через env
docker run --rm \
  -e TELEGRAM_BOT_TOKEN=123:abc \
  -e TELEGRAM_BOT_USERNAME=my_listener_bot \
  -e TELEGRAM_OUTPUT_FORMAT=text \
  telegram-listener
```

Можно также примонтировать файл для логов: `-e TELEGRAM_LOG_FILE=/logs/updates.log -v $(pwd)/logs:/logs`.

PR'ы и задачи приветствуются.
