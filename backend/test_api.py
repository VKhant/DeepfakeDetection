"""
Unit tests for Deepfake Detection AI FastAPI endpoints.
"""
import io
import pytest
from fastapi.testclient import TestClient
from PIL import Image
from main import app

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert "model" in data
    assert "device" in data

def test_analyze_image():
    # Create test image in memory
    img = Image.new("RGB", (200, 200), color=(120, 150, 180))
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    buf.seek(0)

    files = {"file": ("test_sample.jpg", buf, "image/jpeg")}
    response = client.post("/api/v1/analyze-image", files=files)
    assert response.status_code == 200
    data = response.json()
    assert data["file_name"] == "test_sample.jpg"
    assert data["verdict"] in ["LIKELY_REAL", "LIKELY_MANIPULATED", "INCONCLUSIVE"]
    assert 0 <= data["confidence_score"] <= 100
    assert "metrics" in data
    assert "ela_score" in data["metrics"]

def test_unsupported_file_extension():
    buf = io.BytesIO(b"dummy binary data")
    files = {"file": ("document.pdf", buf, "application/pdf")}
    response = client.post("/api/v1/analyze-image", files=files)
    assert response.status_code == 400
    assert "Unsupported format" in response.json()["detail"]
