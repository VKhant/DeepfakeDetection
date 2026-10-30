package com.example.data.repository

import com.example.data.local.DetectionDao
import com.example.data.local.DetectionEntity
import com.example.model.AnalysisReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DetectionRepository(private val dao: DetectionDao) {

    val allReports: Flow<List<AnalysisReport>> = dao.getAllDetections().map { entities ->
        entities.map { it.toReport() }
    }

    suspend fun getReportById(id: Long): AnalysisReport? {
        return dao.getDetectionById(id)?.toReport()
    }

    suspend fun saveReport(report: AnalysisReport): Long {
        val entity = DetectionEntity.fromReport(report)
        return dao.insertDetection(entity)
    }

    suspend fun deleteReport(id: Long) {
        dao.deleteDetectionById(id)
    }

    suspend fun clearHistory() {
        dao.clearAllDetections()
    }
}
