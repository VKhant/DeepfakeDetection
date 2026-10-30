"""
Data schemas for Deepfake Detection AI FastAPI Backend.
"""
from typing import List, Optional
from pydantic import BaseModel, Field

class SuspiciousArtifact(BaseModel):
    label: str = Field(..., description="Name of the detected artifact")
    description: str = Field(..., description="Explanation of why this region is anomalous")
    severity: str = Field("Medium", description="Low, Medium, or High")
    regionDescription: str = Field(..., description="Spatial location of anomaly")
    normX: float = 0.5
    normY: float = 0.5
    normWidth: float = 0.3
    normHeight: float = 0.3

class ForensicMetrics(BaseModel):
    ela_score: float = Field(..., description="Error Level Analysis disparity percentage")
    noise_score: float = Field(..., description="High-frequency noise floor consistency")
    boundary_score: float = Field(..., description="Contour inpainting boundary gradient score")
    chroma_score: float = Field(..., description="Color channel chrominance-luminance distortion")
    temporal_jitter_score: float = Field(0.0, description="Video inter-frame temporal discontinuity")
    synthesis_prob: float = Field(..., description="Calibrated synthetic probability 0..100")

class FrameAnalysisResult(BaseModel):
    frame_index: int
    timestamp_ms: int
    timestamp_formatted: str
    verdict: str  # "LIKELY_REAL", "LIKELY_MANIPULATED", "INCONCLUSIVE"
    confidence_score: float
    anomaly_score: float
    summary: str

class ImageAnalysisResponse(BaseModel):
    file_name: str
    verdict: str  # "LIKELY_REAL", "LIKELY_MANIPULATED", "INCONCLUSIVE"
    confidence_score: float
    model_name: str
    summary: str
    technical_details: str
    frames_analyzed: int = 1
    artifacts: List[SuspiciousArtifact] = []
    metrics: ForensicMetrics

class VideoAnalysisResponse(BaseModel):
    file_name: str
    verdict: str
    confidence_score: float
    model_name: str
    summary: str
    technical_details: str
    frames_analyzed: int
    suspicious_frames_count: int
    artifacts: List[SuspiciousArtifact] = []
    frame_timeline: List[FrameAnalysisResult] = []
    metrics: ForensicMetrics

class HealthResponse(BaseModel):
    status: str
    model: str
    device: str
    version: str
