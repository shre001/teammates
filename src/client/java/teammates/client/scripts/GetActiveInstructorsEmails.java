package teammates.client.scripts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import teammates.client.remoteapi.RemoteApiClient;
import teammates.client.scripts.util.LoopHelper;
import teammates.common.util.TimeHelper;
import teammates.storage.entity.Course;
import teammates.storage.entity.Instructor;

/**
 * Gets the email addresses of all instructors of courses created within the last year.
 */
public class GetActiveInstructorsEmails extends RemoteApiClient {
    private static final Date ONE_YEAR_AGO = TimeHelper.convertInstantToDate(
            TimeHelper.parseInstant("2017-04-01 12:00 AM +0000"));

    public static void main(String[] args) throws IOException {
        new GetActiveInstructorsEmails().doOperationRemotely();
    }

    @Override
    protected void doOperation() {
        println("Getting active courses...");
        List<Course> activeCourses = getActiveCourses();
        println("Number of active courses: " + activeCourses.size());

        println("Getting instructors of active courses...");
        Set<String> emailSet = new HashSet<>();
        List<String> emails = new ArrayList<>();
        LoopHelper loopHelper = new LoopHelper(50, "courses processed.");
        for (Course course : activeCourses) {
            loopHelper.recordLoop();
            List<Instructor> courseInstructors = getInstructorsForCourse(course.getUniqueId());
            for (Instructor instructor : courseInstructors) {
                if (!emailSet.contains(instructor.getEmail())) {
                    emails.add(instructor.getEmail());
                }
                emailSet.add(instructor.getEmail());
            }
        }
        println("Unique email addresses of active instructors sorted by course:" + System.lineSeparator() + emails);
    }

    private List<Course> getActiveCourses() {
        return ofy().load().type(Course.class).filter("createdAt >", ONE_YEAR_AGO).list();
    }

    private List<Instructor> getInstructorsForCourse(String courseId) {
        return ofy().load().type(Instructor.class).filter("courseId =", courseId).list();
    }
}
