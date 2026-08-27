package com.eventtoimpact.india.config;

import com.eventtoimpact.india.model.Event;
import com.eventtoimpact.india.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class EventDataInitializer {
    @Bean
    CommandLineRunner seedEvents(EventRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            List<Event> events = new ArrayList<>();

            addAnnual(events, "National Girl Child Day", "Inclusion",
                    "A nationwide awareness day focused on the rights, education, health, nutrition and opportunities of girls.",
                    1, 24, 1, 24, "Online + campus + community", "Students, parents, educators and youth groups",
                    "Learn about equal opportunity and take part in a school, campus or community awareness activity.", 7,
                    "girls,rights,education,health,inclusion,students,family,awareness", "Press Information Bureau",
                    "https://www.pib.gov.in/PressReleasePage.aspx?PRID=2217624", true, false);

            addAnnual(events, "National Voters' Day", "Civic",
                    "An Election Commission observance that promotes voter enrolment, electoral awareness and informed participation.",
                    1, 25, 1, 25, "Online + public institutions", "New voters, students and all eligible citizens",
                    "Check voter registration, learn election basics and join an electoral-literacy activity.", 8,
                    "voting,democracy,civic,youth,students,rights,awareness", "Election Commission of India",
                    "https://www.eci.gov.in/voicenet/ArticleDECUS.htm", true, false);

            addAnnual(events, "Republic Day Civic Programmes", "National",
                    "A fixed national observance with ceremonies, school programmes and civic-learning activities across India.",
                    1, 26, 1, 26, "Public + campus + broadcast", "Families, students, educators and citizens",
                    "Understand constitutional values and participate through a verified local ceremony or educational programme.", 10,
                    "national,constitution,culture,family,students,civic,public", "MyGov India",
                    "https://blog.mygov.in/editorial/73rd-republic-day-of-india/", true, true);

            addAnnual(events, "National Science Day", "Education",
                    "A science-popularisation day featuring lectures, quizzes, open houses, debates and campus activities.",
                    2, 28, 2, 28, "Campus + science centre + online", "Students, teachers, researchers and curious learners",
                    "Find a science exhibition, open house or quiz and connect classroom learning with real research.", 8,
                    "science,research,innovation,education,students,teachers,technology", "Department of Science and Technology",
                    "https://dst.gov.in/scientific-programmes/st-and-socio-economic-development/national-council-science-technology-communication-ncstc", true, false);

            addAnnual(events, "International Women's Day", "Inclusion",
                    "A global day for equal rights, opportunity, safety and participation for women and girls.",
                    3, 8, 3, 8, "Online + workplace + campus + community", "Women, students, professionals, educators and allies",
                    "Join a rights, leadership, safety or career event and use verified resources to support meaningful action.", 9,
                    "women,rights,career,safety,inclusion,leadership,students,awareness", "United Nations",
                    "https://www.un.org/en/observances/womens-day", true, false);

            addAnnual(events, "World Water Day", "Environment",
                    "A day for water awareness and action on conservation, access, sanitation and the global water crisis.",
                    3, 22, 3, 22, "Online + campus + community", "Students, residents, environmental groups and local institutions",
                    "Audit water use, join a conservation activity or attend a verified learning session on water security.", 7,
                    "water,conservation,climate,environment,students,community,awareness", "United Nations",
                    "https://www.un.org/en/observances/water-day", true, false);

            addAnnual(events, "World Health Day", "Health",
                    "WHO's annual public-health campaign, designed to focus attention on a major health issue and evidence-based action.",
                    4, 7, 4, 7, "Online + health institution + community", "Families, students, health workers and the general public",
                    "Use evidence-based health resources and join a local screening, talk or awareness programme if officially announced.", 9,
                    "health,science,wellbeing,family,students,community,awareness", "World Health Organization",
                    "https://www.who.int/campaigns/world-health-day", true, false);

            addAnnual(events, "International Mother Earth Day", "Environment",
                    "A global observance encouraging environmental learning and practical action for a healthier planet.",
                    4, 22, 4, 22, "Online + campus + community", "Students, families, volunteers and environmental groups",
                    "Choose a measurable action such as waste reduction, a local clean-up or an environmental learning session.", 8,
                    "earth,climate,waste,environment,students,family,volunteering", "United Nations",
                    "https://www.un.org/en/observances/earth-day", true, false);

            addAnnual(events, "National Technology Day", "Technology",
                    "An Indian science and technology observance that recognises innovation, technology development and entrepreneurship.",
                    5, 11, 5, 11, "Campus + innovation hub + online", "Engineering students, researchers, founders and technology professionals",
                    "Attend a technology showcase, innovation talk or student project exhibition from a verified organiser.", 8,
                    "technology,innovation,engineering,startups,research,students,career", "Department of Science and Technology",
                    "https://dst.gov.in/faqs/does-dst-celebrate-special-occasion-felicitate-outstanding-scientist", true, false);

            addAnnual(events, "World Environment Day", "Environment",
                    "The UN's flagship day for environmental outreach, marked through learning, advocacy and community action.",
                    6, 5, 6, 5, "Online + public + campus + community", "Students, families, volunteers, institutions and environmental groups",
                    "Join a verified clean-up, restoration or climate-learning activity and record the action you completed.", 10,
                    "environment,climate,clean-up,nature,students,family,volunteering", "United Nations Environment Programme",
                    "https://www.unep.org/events/un-day/world-environment-day", true, false);

            addAnnual(events, "International Day of Yoga", "Health",
                    "A nationwide wellness observance with common-protocol sessions and programmes organised by institutions and communities.",
                    6, 21, 6, 21, "Public + campus + workplace + online", "Beginners, families, students, professionals and senior citizens",
                    "Use the official common protocol or join a verified local session suited to your health and mobility needs.", 10,
                    "yoga,fitness,wellness,health,family,students,seniors,community", "Ministry of Ayush",
                    "https://yoga.ayush.gov.in/", true, false);

            addAnnual(events, "World Youth Skills Day", "Career",
                    "A skills-focused day highlighting training for employment, decent work, entrepreneurship and responsible citizenship.",
                    7, 15, 7, 15, "Online + campus + training centre", "Students, job seekers, trainees, educators and early-career professionals",
                    "Complete a skills self-audit and attend one verified workshop, career talk or training session.", 9,
                    "skills,career,jobs,training,entrepreneurship,students,youth,technology", "United Nations",
                    "https://www.un.org/en/observances/world-youth-skills-day", true, false);

            addAnnual(events, "National Handloom Day", "Livelihood",
                    "A national observance supporting India's handloom heritage, weavers, sustainable craft and local livelihoods.",
                    8, 7, 8, 7, "Exhibition + online + community", "Families, students, designers, craft buyers and cultural groups",
                    "Learn how to identify handloom products and support a verified artisan, cooperative or public exhibition.", 8,
                    "handloom,craft,culture,livelihood,sustainability,shopping,family", "Development Commissioner for Handlooms",
                    "https://handlooms.nic.in/", true, false);

            addAnnual(events, "Independence Day Community Programmes", "National",
                    "A fixed national observance with flag ceremonies, cultural programmes and civic activities across the country.",
                    8, 15, 8, 15, "Public + campus + broadcast", "Families, students, educators and citizens",
                    "Join a verified local programme and connect the celebration with one practical community responsibility.", 10,
                    "national,culture,history,civic,family,students,public", "MyGov India",
                    "https://www.mygov.in/", true, true);

            addAnnual(events, "National Sports Day", "Health",
                    "A nationwide sports observance encouraging physical activity, participation and recognition of sporting achievement.",
                    8, 29, 8, 29, "Campus + sports venue + community", "Students, families, athletes, beginners and sports groups",
                    "Join a safe local activity, try an inclusive sport or attend a verified campus or community programme.", 9,
                    "sports,fitness,health,students,family,community,wellness", "Ministry of Youth Affairs and Sports",
                    "https://yas.nic.in/sites/default/files/Monthly%20Report%20August%20Isued.pdf", true, false);

            addAnnual(events, "International Literacy Day", "Education",
                    "A UNESCO observance focused on literacy, inclusion, opportunity and meaningful participation in society.",
                    9, 8, 9, 8, "Online + school + library + community", "Learners, students, educators, volunteers and community groups",
                    "Volunteer for reading support, visit a library programme or join a verified digital-literacy activity.", 8,
                    "literacy,reading,education,digital,students,teachers,volunteering", "UNESCO",
                    "https://www.unesco.org/en/days/literacy", true, false);

            addAnnual(events, "Cyber Security Awareness Month", "Safety",
                    "CERT-In's October campaign for cyber-security best practices, safer digital behaviour and scam awareness.",
                    10, 1, 10, 31, "Online + campus + workplace", "Students, families, employees, senior citizens and internet users",
                    "Review account security, enable multi-factor authentication and complete one official cyber-awareness resource.", 9,
                    "cybersecurity,scams,privacy,safety,technology,students,family,career", "CERT-In",
                    "https://www.cert-in.org.in/NCSA.jsp", true, false);

            addAnnual(events, "Gandhi Jayanti Service Activities", "Civic",
                    "A fixed national observance that can be connected with peace, civic responsibility and verified service activities.",
                    10, 2, 10, 2, "Public + campus + community", "Families, students, educators, volunteers and civic groups",
                    "Participate in a verified cleanliness, peace-education or community-service activity rather than treating it only as a holiday.", 9,
                    "service,peace,cleanliness,civic,history,students,family,volunteering", "MyGov India",
                    "https://www.mygov.in/", true, true);

            addAnnual(events, "World Mental Health Day", "Health",
                    "A WHO campaign to raise mental-health awareness, reduce stigma and mobilise support for accessible care.",
                    10, 10, 10, 10, "Online + campus + workplace + health institution", "Students, employees, families, educators and health professionals",
                    "Attend an evidence-based awareness session, learn support pathways and avoid self-diagnosis or unverified advice.", 9,
                    "mental health,wellbeing,students,workplace,family,health,awareness", "World Health Organization",
                    "https://www.who.int/india/campaigns/world-mental-health-day", true, false);

            addAnnual(events, "National Education Day", "Education",
                    "An Indian education observance that encourages learning, access, innovation and discussion of education's public value.",
                    11, 11, 11, 11, "Campus + school + online", "Students, teachers, parents, institutions and lifelong learners",
                    "Join a verified lecture, reading activity, mentoring session or discussion on equitable education.", 8,
                    "education,learning,students,teachers,mentoring,career,awareness", "Ministry of Education",
                    "https://www.education.gov.in/", true, false);

            addAnnual(events, "Constitution Day", "Civic",
                    "Samvidhan Divas commemorates the adoption of India's Constitution and promotes constitutional values among citizens.",
                    11, 26, 11, 26, "Online + public institution + campus", "Students, educators, citizens, public servants and civic groups",
                    "Read the Preamble, complete a verified civic-learning activity and connect rights with fundamental duties.", 9,
                    "constitution,rights,duties,civic,law,students,education,awareness", "MyGov India",
                    "https://www.mygov.in/campaigns/constitution-day", true, false);

            addAnnual(events, "Human Rights Day", "Inclusion",
                    "A global observance for learning about dignity, equality and the rights every person needs to live and thrive.",
                    12, 10, 12, 10, "Online + campus + community", "Students, educators, civil-society groups and all citizens",
                    "Use an official rights resource, join a verified learning event and reflect on inclusion in everyday decisions.", 8,
                    "human rights,equality,inclusion,law,students,education,awareness", "United Nations",
                    "https://www.un.org/en/observances/human-rights-day", true, false);

            repository.saveAll(events);
        };
    }

    private void addAnnual(List<Event> events, String title, String category, String description,
                           int startMonth, int startDay, int endMonth, int endDay,
                           String participationMode, String audience, String impactGoal,
                           int popularity, String tags, String sourceName, String sourceUrl,
                           boolean featured, boolean publicHoliday) {
        for (int year : List.of(2026, 2027)) {
            Event event = new Event();
            event.setTitle(title + " " + year);
            event.setCategory(category);
            event.setDescription(description);
            event.setVenue("Verified programmes announced by local organisers");
            event.setArea("Nationwide");
            event.setCity("India");
            event.setStartTime(LocalDateTime.of(year, startMonth, startDay, 9, 0));
            event.setEndTime(LocalDateTime.of(year, endMonth, endDay, 18, 0));
            event.setRecurringRule("FIXED_ANNUAL_OBSERVANCE");
            event.setPrice(0);
            event.setFreeEntry(true);
            event.setIndoor(participationMode.contains("Online") || participationMode.contains("Campus"));
            event.setCapacity(0);
            event.setExpectedAttendance(0);
            event.setAttendanceBasis("NO_LIVE_COUNT");
            event.setPopularityScore(popularity);
            event.setFamilyFriendly(tags.contains("family"));
            event.setStudentFriendly(tags.contains("students"));
            event.setSeniorFriendly(true);
            event.setAccessible(participationMode.contains("Online"));
            event.setNoiseLevel(publicHoliday ? "MODERATE" : "LOW");
            event.setWalkingLevel("LOW");
            event.setTags(tags);
            event.setAudience(audience);
            event.setParticipationMode(participationMode);
            event.setImpactGoal(impactGoal);
            event.setPublicHoliday(publicHoliday);
            event.setFeatured(featured);
            event.setSourceName(sourceName);
            event.setSourceUrl(sourceUrl);
            event.setVerifiedAt(LocalDateTime.of(2026, 8, 2, 12, 0));
            event.setVerificationStatus(year == 2026
                    ? "ANNUAL_DATE_VERIFIED_LOCAL_PROGRAMMES_VARY"
                    : "ANNUAL_DATE_VERIFIED_PROGRAMME_DETAILS_TBA");
            event.setDateStatus("FIXED_ANNUAL_DATE");
            events.add(event);
        }
    }
}
