data class Course(
    val id: Long,
    val classCode: String,
    val courseName: String,
    val credit: Double,
    val hours: Int,
    val description: String? = null,
    val createdAt: String? = null
)

data class TeachingClass(
    val id: Long,
    val classCode: String,
    val maxStudents: Int,
    val currentStudents: Int,
    val course: Course,
    val teacher: Teacher? = null
)

data class Teacher(
    val id: Long,
    val title: String,
    val department: String,
    val user: User? = null
)

data class Student(
    val id: Long,
    val grade: String,
    val className: String,
    val user: User? = null
)

data class Classroom(
    val id: Long,
    val building: String,
    val classroomName: String,
    val capacity: Int
)

data class ClassSchedule(
    val id: Long,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val classroom: Classroom,
    val teachingClass: TeachingClass
)

data class CourseSelection(
    val id: Long,
    val student: Student,
    val teachingClass: TeachingClass,
    val selectionTime: String? = null
)

data class SelectionDTO(
    val id: Long,
    val studentId: Long,
    val studentName: String,
    val teachingClassId: Long,
    val courseName: String,
    val teacherName: String,
    val selectionTime: String? = null
)

data class MyCourseDTO(
    val courseId: Long,
    val courseName: String,
    val credit: Double,
    val teacherName: String,
    val classCode: String,
    val selectionTime: String? = null
)