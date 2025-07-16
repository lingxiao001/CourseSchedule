package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.coursescheduleapp.model.CourseWithTeachingClassesDTO
import com.example.coursescheduleapp.viewmodel.CourseSelectionViewModel


/**
 * 课程选择底部弹窗包装器
 * 处理底部弹窗的显示和状态管理
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionBottomSheetWrapper(
    course: CourseWithTeachingClassesDTO,
    studentId: Long,
    onDismiss: () -> Unit,
    onSelectionChanged: (Long, Boolean) -> Unit,
    selectionViewModel: CourseSelectionViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        CourseSelectionBottomSheet(
            course = course,
            studentId = studentId,
            onDismiss = onDismiss,
            onSelectionChanged = onSelectionChanged,
            selectionViewModel = selectionViewModel
        )
    }
}