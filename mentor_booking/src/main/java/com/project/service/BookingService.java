package com.project.service;

import com.project.dto.BookingDTO;
import com.project.dto.ClassDTO;
import com.project.dto.Response;
import com.project.enums.AvailableStatus;
import com.project.enums.BookingStatus;
import com.project.enums.PointHistoryStatus;
import com.project.exception.OurException;
import com.project.model.Booking;
import com.project.model.Group;
import com.project.model.MentorSchedule;
import com.project.model.Mentors;
import com.project.model.PointHistory;
import com.project.model.Students;
import com.project.repository.BookingRepository;
import com.project.repository.GroupRepository;
import com.project.repository.MentorScheduleRepository;
import com.project.repository.MentorsRepository;
import com.project.repository.PointHistoryRepository;
import com.project.ultis.Converter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Thịnh Đạt
 */
@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MentorScheduleRepository mentorScheduleRepository;

    @Autowired
    private MentorsRepository mentorsRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    public Response createBooking(BookingDTO createRequest) {
        Response response = new Response();
        try {
            if (createRequest.getMentorSchedule() == null) {
                throw new OurException("Cannot find schedule");
            }
            //The group has already have a confirmed booking
//            if (bookingRepository.findByGroupIdAndAvailableStatusAndStatus(createRequest.getGroup().getId(), AvailableStatus.ACTIVE, BookingStatus.CONFIRMED) != null) {
//                throw new OurException("You already have a booking that is confirmed");
//            }

            //This mentor has accepted another group's booking
//            if (bookingRepository.findByAvailableStatusAndStatusAndMentorScheduleId(AvailableStatus.ACTIVE, BookingStatus.CONFIRMED, createRequest.getMentorSchedule().getId()) != null) {
//                throw new OurException("The mentor has already have a meeting with this schedule");
//            }

            //This group has already booked this mentor with the same schedule, prevent spamming
//            if (bookingRepository.findByAvailableStatusAndStatusAndMentorScheduleIdAndGroupId(AvailableStatus.ACTIVE, BookingStatus.PENDING,
//                    createRequest.getMentorSchedule().getId(), createRequest.getGroup().getId()) != null) {
//                throw new OurException("You have booked this mentor with this schedule");
//            }

            MentorSchedule mentorSchedule = mentorScheduleRepository.findByIdAndAvailableStatus(createRequest.getMentorSchedule().getId(), AvailableStatus.ACTIVE);
            Mentors mentor = mentorsRepository.findByIdAndAvailableStatus(mentorSchedule.getMentor().getId(), AvailableStatus.ACTIVE);
            Group group = groupRepository.findByIdAndAvailableStatus(createRequest.getGroup().getId(), AvailableStatus.ACTIVE);

            LocalDateTime timeStart = mentorSchedule.getAvailableFrom();
            LocalDateTime timeEnd = mentorSchedule.getAvailableTo();
            int time = (int) timeStart.until(timeEnd, ChronoUnit.MINUTES);
            time /= 30;

            int pointPay = group.getStudents().size() * 10 * (int) time;

            Booking booking = new Booking();
            booking.setDateCreated(LocalDateTime.now());
            booking.setDateUpdated(LocalDateTime.now());
            booking.setStatus(BookingStatus.PENDING);
            booking.setMentorSchedule(mentorSchedule);
            booking.setMentor(mentor);
            booking.setGroup(group);
            booking.setPointPay(pointPay);
            booking.setAvailableStatus(AvailableStatus.ACTIVE);
            bookingRepository.save(booking);

            if (booking.getId() > 0) {
                BookingDTO dto = Converter.convertBookingToBookingDTO(booking);
                response.setBookingDTO(dto);
                response.setStatusCode(200);
                response.setMessage("Create booking successfully");
            }

        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during booking creation: " + e.getMessage());
        }
        return response;
    }

    /**
     * Get all ACTIVE bookings (usually PENDING and not started CONFIRMED
     * bookings)
     *
     * @return all bookings have ACTIVE available status
     */
    public Response getAllActiveBookings() {
        Response response = new Response();
        try {
            List<Booking> bookingList = bookingRepository.findByAvailableStatus(AvailableStatus.ACTIVE);
            List<BookingDTO> bookingListDTO = new ArrayList<>();
            if (!bookingList.isEmpty()) {
                bookingListDTO = bookingList.stream()
                        .map(Converter::convertBookingToBookingDTO)
                        .collect(Collectors.toList());

                response.setBookingDTOList(bookingListDTO);
                response.setStatusCode(200);
                response.setMessage("Bookings fetched successfully");
            } else {
                response.setBookingDTOList(bookingListDTO);
                response.setStatusCode(400);
                response.setMessage("Cannot find any booking");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during get all bookings: " + e.getMessage());
        }
        return response;
    }

    /**
     * Get all INACTIVE bookings (Usually done CONFIRMED, REJECTED or CANCELED
     * bookings)
     *
     * @return all INACTIVE bookings (or in another way, all CONFIRMED bookings
     * that have been started before and REJECTED, CANCELED bookings)
     */
    public Response getAllOldBookings() {
        Response response = new Response();
        try {
            List<Booking> bookingList = bookingRepository.findByAvailableStatus(AvailableStatus.INACTIVE);
            List<BookingDTO> bookingListDTO = new ArrayList<>();
            if (!bookingList.isEmpty()) {
                bookingListDTO = bookingList.stream()
                        .map(Converter::convertBookingToBookingDTO)
                        .collect(Collectors.toList());

                response.setBookingDTOList(bookingListDTO);
                response.setStatusCode(200);
                response.setMessage("Bookings fetched successfully");
            } else {
                response.setBookingDTOList(bookingListDTO);
                response.setStatusCode(400);
                response.setMessage("Cannot find any booking");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during get all bookings: " + e.getMessage());
        }
        return response;
    }

    public Response getBookingsInClass(Long classId) {
        Response response = new Response();
        try {
            List<Booking> bookingList = bookingRepository.findAllByClassIdAndAvailableStatus(classId, AvailableStatus.ACTIVE);
            List<BookingDTO> bookingListDTO = new ArrayList<>();
            if (!bookingList.isEmpty()) {
                bookingListDTO = bookingList.stream()
                        .map(Converter::convertBookingToBookingDTO)
                        .collect(Collectors.toList());

                response.setBookingDTOList(bookingListDTO);
                response.setStatusCode(200);
                response.setMessage("Bookings fetched successfully");
            } else {
                response.setBookingDTOList(bookingListDTO);
                response.setStatusCode(400);
                response.setMessage("Cannot find any booking");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during get all bookings: " + e.getMessage());
        }
        return response;
    }

    public Response acceptBooking(Long bookingId) {
        Response response = new Response();
        try {
            Booking booking = bookingRepository.findByIdAndAvailableStatusAndStatus(bookingId, AvailableStatus.ACTIVE, BookingStatus.PENDING);
            if (booking != null) {
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setDateUpdated(LocalDateTime.now());

                PointHistory pointHistory = new PointHistory();
                pointHistory.setStatus(PointHistoryStatus.REDEEMED);
                pointHistory.setAvailableStatus(AvailableStatus.ACTIVE);
                pointHistory.setBooking(booking);
                pointHistory.setDateCreated(LocalDateTime.now());
                pointHistory.setDateUpdated(LocalDateTime.now());
                pointHistory.setPoint(booking.getPointPay());
                pointHistoryRepository.save(pointHistory);

                List<PointHistory> pointHistoryList;
                if (booking.getPointHistories() == null) {
                    pointHistoryList = new ArrayList<>();
                } else {
                    pointHistoryList = booking.getPointHistories();
                }
                pointHistoryList.add(pointHistory);
                booking.setPointHistories(pointHistoryList);

                List<Students> groupMembers = booking.getGroup().getStudents();
                int pointMinusForAllMembers = booking.getPointPay() / groupMembers.size();
                for (Students member : groupMembers) {
                    member.setPoint(member.getPoint() - pointMinusForAllMembers);
                }
                Group group = groupRepository.findByIdAndAvailableStatus(booking.getGroup().getId(), AvailableStatus.ACTIVE);
                group.setStudents(groupMembers);
                group.setTotalPoint(group.getTotalPoint() - booking.getPointPay());

                groupRepository.save(group);
                bookingRepository.save(booking);

                BookingDTO dto = Converter.convertBookingToBookingDTO(booking);
                response.setBookingDTO(dto);
                response.setStatusCode(200);
                response.setMessage("Booking accepted");
            } else {
                throw new OurException("Cannot find booking");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during accept booking: " + e.getMessage());
        }
        return response;
    }

    public Response rejectBooking(Long bookingId) {
        Response response = new Response();
        try {
            Booking booking = bookingRepository.findByIdAndAvailableStatusAndStatus(bookingId, AvailableStatus.ACTIVE, BookingStatus.PENDING);
            if (booking != null) {
                booking.setStatus(BookingStatus.REJECTED);
                booking.setAvailableStatus(AvailableStatus.INACTIVE);
                booking.setDateUpdated(LocalDateTime.now());

                bookingRepository.save(booking);

                BookingDTO dto = Converter.convertBookingToBookingDTO(booking);
                response.setBookingDTO(dto);
                response.setStatusCode(200);
                response.setMessage("Booking rejected");
            } else {
                throw new OurException("Cannot find booking");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during reject booking: " + e.getMessage());
        }
        return response;
    }

    public Response cancelBooking(Long bookingId, String type) {
        Response response = new Response();
        try {
            Booking booking = bookingRepository.findByIdAndAvailableStatusAndStatus(bookingId, AvailableStatus.ACTIVE, BookingStatus.CONFIRMED);
            if (booking != null) {
                if (type.equalsIgnoreCase("MENTOR")) {
                    booking.setStatus(BookingStatus.CANCELLED);
                    booking.setAvailableStatus(AvailableStatus.INACTIVE);
                    booking.setDateUpdated(LocalDateTime.now());

                    PointHistory pointHistory = new PointHistory();
                    pointHistory.setStatus(PointHistoryStatus.ADJUSTED);
                    pointHistory.setAvailableStatus(AvailableStatus.ACTIVE);
                    pointHistory.setBooking(booking);
                    pointHistory.setDateCreated(LocalDateTime.now());
                    pointHistory.setDateUpdated(LocalDateTime.now());
                    pointHistory.setPoint(booking.getPointPay());
                    pointHistoryRepository.save(pointHistory);

                    List<PointHistory> pointHistoryList;
                    if (booking.getPointHistories() == null) {
                        pointHistoryList = new ArrayList<>();
                    } else {
                        pointHistoryList = booking.getPointHistories();
                    }
                    pointHistoryList.add(pointHistory);
                    booking.setPointHistories(pointHistoryList);

                    List<Students> groupMembers = booking.getGroup().getStudents();
                    int pointPlusForAllMembers = booking.getPointPay() / groupMembers.size();
                    for (Students member : groupMembers) {
                        member.setPoint(member.getPoint() + pointPlusForAllMembers);
                    }
                    Group group = groupRepository.findByIdAndAvailableStatus(booking.getGroup().getId(), AvailableStatus.ACTIVE);
                    group.setStudents(groupMembers);
                    group.setTotalPoint(group.getTotalPoint() + booking.getPointPay());

                    groupRepository.save(group);
                    bookingRepository.save(booking);

                    BookingDTO dto = Converter.convertBookingToBookingDTO(booking);
                    response.setBookingDTO(dto);
                    response.setStatusCode(200);
                    response.setMessage("Booking canceled by mentor");
                }
                if (type.equalsIgnoreCase("STUDENT")) {
                    booking.setStatus(BookingStatus.CANCELLED);
                    booking.setAvailableStatus(AvailableStatus.INACTIVE);
                    booking.setDateUpdated(LocalDateTime.now());

                    PointHistory pointHistory = new PointHistory();
                    pointHistory.setStatus(PointHistoryStatus.EXPIRED);
                    pointHistory.setAvailableStatus(AvailableStatus.ACTIVE);
                    pointHistory.setBooking(booking);
                    pointHistory.setDateCreated(LocalDateTime.now());
                    pointHistory.setDateUpdated(LocalDateTime.now());
                    pointHistory.setPoint(booking.getPointPay());
                    pointHistoryRepository.save(pointHistory);

                    List<PointHistory> pointHistoryList;
                    if (booking.getPointHistories() == null) {
                        pointHistoryList = new ArrayList<>();
                    } else {
                        pointHistoryList = booking.getPointHistories();
                    }
                    pointHistoryList.add(pointHistory);
                    booking.setPointHistories(pointHistoryList);
                    
                    bookingRepository.save(booking);

                    BookingDTO dto = Converter.convertBookingToBookingDTO(booking);
                    response.setBookingDTO(dto);
                    response.setStatusCode(200);
                    response.setMessage("Booking canceled by students");
                }
            } else {
                throw new OurException("Cannot find booking");
            }
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred during cancel booking: " + e.getMessage());
        }
        return response;
    }
}
