# Deepfake Detection AI - Inference Backend

"Detect the Fake. Trust the Real."

This directory contains the Python FastAPI inference backend for **Deepfake Detection AI**, providing real-time forensic image and video analysis for synthetic media, GAN generation, face-swaps, and diffusion artifacts.

---

## 1. Quick Start

### Prerequisites
- Python 3.10+
- `pip` and virtual environment tool (`venv` or `conda`)
- Optional: CUDA-capable GPU for accelerated PyTorch inference

### Setup
```bash
# 1. Navigate to backend directory
cd backend

# 2. Create virtual environment
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# 3. Install dependencies
pip install -r requirements.txt
```

### Running the Server
```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```
Once started, visit:
- **Interactive Swagger Docs:** [http://localhost:8000/docs](http://localhost:8000/docs)
- **Health Check:** [http://localhost:8000/health](http://localhost:8000/health)

---

## 2. API Endpoints

### `GET /health`
Returns system status, active compute device (`cuda` or `cpu`), and model version.

### `POST /api/v1/analyze-image`
- **Request:** `multipart/form-data` with `file: UploadFile` (JPG, JPEG, PNG, WEBP).
- **Response:** JSON with verdict (`LIKELY_REAL`, `LIKELY_MANIPULATED`, `INCONCLUSIVE`), calibrated confidence score, telemetry metrics (ELA, Laplacian noise, boundary seams), and pinpointed artifact bounding coordinates.

### `POST /api/v1/analyze-video`
- **Request:** `multipart/form-data` with `file: UploadFile` (MP4, MOV, WEBM) and query parameter `frame_interval` (default 1.0s).
- **Response:** JSON with aggregated video verdict, frame-by-frame findings timeline, suspicious frames count, and inter-frame temporal jitter metrics.

---

## 3. Automated Testing
Run pytest:
```bash
pytest test_api.py -v
```

---

## 4. Connecting Android App to this Backend
1. When running the Android app in the Android Emulator, the local development machine is accessible at `http://10.0.2.2:8000`.
2. In the Android app, navigate to **About & Settings** tab.
3. Toggle on **Remote FastAPI Backend**.
4. Confirm `http://10.0.2.2:8000` and tap **Test Health Check**.
5. The Android client will now route image and video analysis directly through your FastAPI PyTorch backend!

---

## 5. Security & Privacy
- Temporary video files are automatically deleted after processing in a `finally:` block.
- Maximum upload limits (25MB for images, 100MB for video) prevent denial-of-service memory pressure.
- Strict extension validation against MIME spoofing.
