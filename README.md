# Video Highlight Cutter

Spring Boot MVC tool de keo tha 1 video vao web app, server tu phan tich chuyen canh, cat cac doan noi bat, ghep lai thanh video doc 9:16 va cho tai xuong.

Flow chinh:

- Keo tha/chon 1 file video tren giao dien web.
- Server luu file vao `media/jobs/<job-id>/upload`.
- `ffmpeg` phan tich scene/chuyen canh va cat cac doan noi bat.
- Server ghep thanh `highlight.mp4`.
- Bam nut tai xuong.
- Sau khi stream tai xuong xong, server tu xoa job folder, gom video goc da upload va file tam.

## Luu y ve ban quyen

Dung video ban tu quay, video ban so huu, hoac stock/Creative Commons co license phu hop. Tool co endpoint tai video nhung chi chap nhan URL tu Pexels/Pixabay de tranh lay video khong ro quyen su dung.

## Cai dat

1. Cai Java 11+ va ffmpeg.
2. Dam bao lenh `ffmpeg -version` va `ffprobe -version` chay duoc trong terminal.
Neu ffmpeg khong nam trong PATH, sua `src/main/resources/application.properties`:

```properties
app.media.ffmpeg-path=C:/ffmpeg/bin/ffmpeg.exe
app.media.ffprobe-path=C:/ffmpeg/bin/ffprobe.exe
```

## Chay app

Tu thu muc cha `C:\Users\minhm\OneDrive\Documents\tool`:

```powershell
.\run-dev.cmd
```

Mo:

```text
http://localhost:8080
```

## API nhanh

Kiem tra he thong:

```http
GET /api/health
```

Tim video local:

```http
POST /api/highlights
Content-Type: multipart/form-data

video=<file>
clipCount=6
clipSeconds=3.5
```

Response tra ve `downloadUrl`, vi du `/api/highlights/<job-id>/download`.
