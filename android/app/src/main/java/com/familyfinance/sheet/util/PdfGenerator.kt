package com.familyfinance.sheet.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.familyfinance.sheet.data.model.CategoryGroup
import com.familyfinance.sheet.data.model.CategoryType
import com.familyfinance.sheet.data.model.ReportData
import com.familyfinance.sheet.data.model.ReportItem
import com.familyfinance.sheet.data.model.Summary
import com.familyfinance.sheet.data.model.SummaryComparison
import com.familyfinance.sheet.data.model.ViewMode
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PDF 生成器
 * 用于生成财务报表 PDF 文件
 */
class PdfGenerator(private val context: Context) {
    
    private val numberFormat = NumberFormat.getNumberInstance(Locale.CHINA)
    
    // 页面尺寸 (A4: 595 x 842 点)
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 40f
    private val contentWidth = pageWidth - 2 * margin
    
    // 画笔
    private val titlePaint = Paint().apply {
        color = Color.parseColor("#1E293B")
        textSize = 24f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    
    private val subtitlePaint = Paint().apply {
        color = Color.parseColor("#64748B")
        textSize = 12f
        isAntiAlias = true
    }
    
    private val headerPaint = Paint().apply {
        color = Color.parseColor("#4F46E5")
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    
    private val textPaint = Paint().apply {
        color = Color.parseColor("#334155")
        textSize = 12f
        isAntiAlias = true
    }
    
    private val valuePaint = Paint().apply {
        color = Color.parseColor("#1E293B")
        textSize = 12f
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
    }
    
    private val linePaint = Paint().apply {
        color = Color.parseColor("#E2E8F0")
        strokeWidth = 1f
    }
    
    private val summaryBgPaint = Paint().apply {
        color = Color.parseColor("#F1F5F9")
    }
    
    /**
     * 生成 PDF 文件
     */
    fun generatePdf(
        reportData: ReportData,
        viewMode: ViewMode,
        period: String,
        summary: Summary,
        comparisonPeriod: String = "",
        summaryComparison: SummaryComparison? = null
    ): File? {
        val document = PdfDocument()
        
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            var yPos = margin
            
            // 绘制标题
            val title = if (viewMode == ViewMode.DAY) "资产负债表" else "利润表"
            canvas.drawText(title, margin, yPos + 24f, titlePaint)
            yPos += 35f
            
            // 绘制副标题
            canvas.drawText("FAMILY FINANCE - $period", margin, yPos + 12f, subtitlePaint)
            yPos += 30f
            
            // 绘制生成日期
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
            canvas.drawText("生成日期: ${dateFormat.format(Date())}", margin, yPos + 12f, subtitlePaint)
            yPos += 40f
            
            // 绘制汇总区域
            yPos = drawSummarySection(
                canvas = canvas,
                startY = yPos,
                viewMode = viewMode,
                summary = summary,
                comparisonPeriod = comparisonPeriod,
                summaryComparison = summaryComparison
            )
            yPos += 20f
            
            // 绘制分组数据
            val displayGroups = reportData.getDisplayGroups(viewMode, period)
            for (group in displayGroups) {
                yPos = drawCategoryGroup(canvas, yPos, group, period)
                yPos += 15f
                
                // 检查是否需要新页面
                if (yPos > pageHeight - 100) {
                    break // 简化处理，仅支持单页
                }
            }
            
            document.finishPage(page)
            
            // 保存文件
            val fileName = "${title}_${period.replace("-", "")}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            document.close()
        }
    }
    
    /**
     * 绘制汇总区域
     */
    private fun drawSummarySection(
        canvas: Canvas,
        startY: Float,
        viewMode: ViewMode,
        summary: Summary,
        comparisonPeriod: String,
        summaryComparison: SummaryComparison?
    ): Float {
        var yPos = startY
        val hasComparison = comparisonPeriod.isNotBlank() && summaryComparison != null
        val sectionHeight = if (hasComparison) 125f else 80f
        
        // 背景
        canvas.drawRect(margin, yPos, pageWidth - margin, yPos + sectionHeight, summaryBgPaint)
        
        yPos += 20f
        
        if (viewMode == ViewMode.DAY) {
            // 资产负债表汇总
            canvas.drawText("资产总额", margin + 10f, yPos + 14f, headerPaint)
            canvas.drawText("¥ ${numberFormat.format(summary.asset)}", margin + 150f, yPos + 14f, valuePaint.apply { 
                color = Color.parseColor("#4F46E5")
            })
            
            canvas.drawText("负债总额", margin + 200f, yPos + 14f, headerPaint)
            canvas.drawText("¥ ${numberFormat.format(summary.liability)}", margin + 350f, yPos + 14f, valuePaint.apply {
                color = Color.parseColor("#D97706")
            })
            
            yPos += 30f
            
            canvas.drawText("所有者权益（净资产）", margin + 10f, yPos + 14f, headerPaint)
            val netWorthColor = if (summary.netWorth >= 0) Color.parseColor("#059669") else Color.parseColor("#E11D48")
            canvas.drawText("¥ ${numberFormat.format(summary.netWorth)}", margin + 250f, yPos + 14f, valuePaint.apply {
                color = netWorthColor
            })
            
            if (hasComparison) {
                yPos += 30f
                textPaint.color = Color.parseColor("#475569")
                canvas.drawText(
                    "较 $comparisonPeriod：资产 ${formatDelta(summaryComparison!!.asset.delta, summaryComparison.asset.percentChange)}",
                    margin + 10f,
                    yPos + 12f,
                    textPaint
                )
                canvas.drawText(
                    "净资产 ${formatDelta(summaryComparison.netWorth.delta, summaryComparison.netWorth.percentChange)}",
                    margin + 280f,
                    yPos + 12f,
                    textPaint
                )
            }
        } else {
            // 利润表汇总
            canvas.drawText("本期收入", margin + 10f, yPos + 14f, headerPaint)
            canvas.drawText("¥ ${numberFormat.format(summary.income)}", margin + 150f, yPos + 14f, valuePaint.apply {
                color = Color.parseColor("#059669")
            })
            
            canvas.drawText("本期支出", margin + 200f, yPos + 14f, headerPaint)
            canvas.drawText("¥ ${numberFormat.format(summary.expense)}", margin + 350f, yPos + 14f, valuePaint.apply {
                color = Color.parseColor("#E11D48")
            })
            
            yPos += 30f
            
            canvas.drawText("本期利润（结余）", margin + 10f, yPos + 14f, headerPaint)
            val profitColor = if (summary.cashflow >= 0) Color.parseColor("#059669") else Color.parseColor("#E11D48")
            canvas.drawText("¥ ${numberFormat.format(summary.cashflow)}", margin + 250f, yPos + 14f, valuePaint.apply {
                color = profitColor
            })
            
            if (hasComparison) {
                yPos += 30f
                textPaint.color = Color.parseColor("#475569")
                canvas.drawText(
                    "较 $comparisonPeriod：收入 ${formatDelta(summaryComparison!!.income.delta, summaryComparison.income.percentChange)}",
                    margin + 10f,
                    yPos + 12f,
                    textPaint
                )
                canvas.drawText(
                    "结余 ${formatDelta(summaryComparison.cashflow.delta, summaryComparison.cashflow.percentChange)}",
                    margin + 280f,
                    yPos + 12f,
                    textPaint
                )
            }
        }
        
        return startY + sectionHeight + 10f
    }
    
    /**
     * 绘制分类组
     */
    private fun drawCategoryGroup(canvas: Canvas, startY: Float, group: CategoryGroup, period: String): Float {
        var yPos = startY
        
        // 分组标题
        val groupColor = when (group.type) {
            CategoryType.INCOME -> Color.parseColor("#059669")
            CategoryType.EXPENSE -> Color.parseColor("#E11D48")
            CategoryType.ASSET -> Color.parseColor("#4F46E5")
            CategoryType.LIABILITY -> Color.parseColor("#D97706")
        }
        
        val groupName = when (group.type) {
            CategoryType.INCOME -> "收入 (Income)"
            CategoryType.EXPENSE -> "支出 (Expenses)"
            CategoryType.ASSET -> "资产 (Assets)"
            CategoryType.LIABILITY -> "负债 (Liabilities)"
        }
        
        headerPaint.color = groupColor
        canvas.drawText(groupName, margin, yPos + 14f, headerPaint)
        yPos += 25f
        
        // 分隔线
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 5f
        
        // 表头
        canvas.drawText("科目", margin, yPos + 12f, subtitlePaint)
        subtitlePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("金额", pageWidth - margin, yPos + 12f, subtitlePaint)
        subtitlePaint.textAlign = Paint.Align.LEFT
        yPos += 20f
        
        // 科目列表
        for (item in group.items) {
            yPos = drawReportItem(canvas, item, period, yPos, depth = 0)
        }
        
        // 小计
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 5f
        val total = group.getTotal(period)
        headerPaint.color = Color.parseColor("#1E293B")
        canvas.drawText("小计", margin, yPos + 14f, headerPaint)
        valuePaint.color = groupColor
        canvas.drawText("¥ ${numberFormat.format(total)}", pageWidth - margin, yPos + 14f, valuePaint)
        
        return yPos + 20f
    }
    
    private fun drawReportItem(
        canvas: Canvas,
        item: ReportItem,
        period: String,
        startY: Float,
        depth: Int
    ): Float {
        var yPos = startY
        val labelX = margin + depth * 16f
        val prefix = if (depth == 0) "" else "└ "
        val value = item.getValue(period)
        canvas.drawText("${prefix}${item.name}", labelX, yPos + 12f, textPaint)
        canvas.drawText(
            "¥ ${numberFormat.format(value)}",
            pageWidth - margin,
            yPos + 12f,
            valuePaint.apply { color = Color.parseColor("#1E293B") }
        )
        yPos += 18f
        
        item.children?.forEach { child ->
            yPos = drawReportItem(canvas, child, period, yPos, depth + 1)
        }
        return yPos
    }
    
    /**
     * 分享 PDF 文件
     */
    fun sharePdf(file: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    /**
     * 打开 PDF 文件
     */
    fun openPdf(file: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    private fun formatDelta(delta: Double, percent: Double?): String {
        val sign = if (delta > 0) "+" else ""
        val percentText = percent?.let {
            NumberFormat.getPercentInstance(Locale.CHINA).apply {
                minimumFractionDigits = 0
                maximumFractionDigits = 2
            }.format(it)
        } ?: "--"
        return "${sign}${numberFormat.format(delta)} / $percentText"
    }
}
