with OverlappingRooms as (
    select distinct Room
    from Reservations
    where (CheckIn < '2010-09-08' and '2010-09-08' <= CheckOut)
        or (CheckIn < '2010-09-10' and '2010-09-10' <= CheckOut)
), OverlappingRooms_ShiftRight as (
    select distinct Room
    from Reservations
    where (CheckIn < date_add('2010-09-08', interval 1 day) and date_add('2010-09-08', interval 1 day) <= CheckOut)
        or (CheckIn < date_add('2010-09-10', interval 1 day) and date_add('2010-09-10', interval 1 day) <= CheckOut)
), OverlappingRooms_ShiftLeft as (
    select distinct Room
    from Reservations
    where (CheckIn < date_add('2010-09-08', interval -1 day) and date_add('2010-09-08', interval -1 day) <= CheckOut)
        or (CheckIn < date_add('2010-09-10', interval -1 day) and date_add('2010-09-10', interval -1 day) <= CheckOut)
), OverlappingRooms_ShortenEnd as (
    select distinct Room
    from Reservations
    where (CheckIn < '2010-09-08' and '2010-09-08' < CheckOut)
        or (CheckIn < date_add('2010-09-10', interval -1 day) and date_add('2010-09-10', interval -1 day) <= CheckOut)
), OverlappingRooms_ShortenStart as (
    select distinct Room
    from Reservations
    where (CheckIn < date_add('2010-09-08', interval 1 day) and date_add('2010-09-08', interval 1 day) <= CheckOut)
        or (CheckIn < '2010-09-10' and '2010-09-10' < CheckOut)
), OverlappingRooms_ShiftRightTwo as (
    select distinct Room
    from Reservations
    where (CheckIn < date_add('2010-09-08', interval 2 day) and date_add('2010-09-08', interval 2 day) <= CheckOut)
        or (CheckIn < date_add('2010-09-10', interval 2 day) and date_add('2010-09-10', interval 2 day) <= CheckOut)
), OverlappingRooms_ShiftLeftTwo as (
    select distinct Room
    from Reservations
    where (CheckIn < date_add('2010-09-08', interval -2 day) and date_add('2010-09-08', interval -2 day) <= CheckOut)
        or (CheckIn < date_add('2010-09-10', interval -2 day) and date_add('2010-09-10', interval -2 day) <= CheckOut)
), AvailableRooms as (
    select roomId as Room,
        '2010-09-08' as CheckIn,
        '2010-09-10' as CheckOut,
        0 as Priority
    from Rooms
    where roomId not in (
        select Room from OverlappingRooms
    )
), AvailableRooms_ShiftRight as (
    select roomId as Room,
        date_add('2010-09-08', interval 1 day) as CheckIn,
        date_add('2010-09-10', interval 1 day) as CheckOut,
        2 as Priority
    from Rooms
    where roomId not in (
        select Room from OverlappingRooms_ShiftRight
    )
), AvailableRooms_ShiftLeft as (
    select roomId as Room,
        date_add('2010-09-08', interval -1 day) as CheckIn,
        date_add('2010-09-10', interval -1 day) as CheckOut,
        2 as Priority
    from Rooms
    where roomId not in (
        select Room from OverlappingRooms_ShiftLeft
    )
), AvailableRooms_ShortenEnd as (
    select roomId as Room,
        '2010-09-08' as CheckIn,
        date_add('2010-09-10', interval -1 day) as CheckOut,
        1 as Priority
    from Rooms
    where roomId not in (
        select Room from OverlappingRooms_ShortenEnd
    )
), AvailableRooms_ShortenStart as (
    select roomId as Room,
        date_add('2010-09-08', interval 1 day) as CheckIn,
        '2010-09-10' as CheckOut,
        1 as Priority
    from Rooms
    where roomId not in (
        select Room from OverlappingRooms_ShortenStart
    )
), AvailableRooms_ShiftRightTwo as (
    select roomId as Room,
        date_add('2010-09-08', interval 2 day) as CheckIn,
        date_add('2010-09-10', interval 2 day) as CheckOut,
        3 as Priority
    from Rooms
    where roomId not in (
        select Room from OverlappingRooms_ShiftRightTwo
    )
), AvailableRooms_ShiftLeftTwo as (
    select roomId as Room,
        date_add('2010-09-08', interval -2 day) as CheckIn,
        date_add('2010-09-10', interval -2 day) as CheckOut,
        3 as Priority
    from Rooms
    where roomId not in (
        select Room from OverlappingRooms_ShiftLeftTwo
    )
), AllAvailByPriority as (
    select *
    from AvailableRooms
    union
    select *
    from AvailableRooms_ShiftRight
    union
    select *
    from AvailableRooms_ShiftLeft
    union
    select *
    from AvailableRooms_ShortenEnd
    union
    select *
    from AvailableRooms_ShortenStart
    union
    select *
    from AvailableRooms_ShiftRightTwo
    union
    select *
    from AvailableRooms_ShiftLeftTwo
), AllAvailPlusInfo as (
    select * 
    from AllAvailByPriority, Rooms
    where roomId = Room
    order by Priority, Room
), CheckOccupancy as (
    select Room, CheckIn, CheckOut, Priority, bedType
    from AllAvailPlusInfo
    where maxOccupancy >= 2
), FuzzyMatch as (
    select *
    from CheckOccupancy
), FilterBed as (
    select *
    from CheckOccupancy
    where bedType like "KING"
), FilterRoom as (
    select *
    from FilterBed
    where Room like "FNA"
), ExactMatch as (
    select *
    from FilterRoom
    where Priority = 0
)
select *
from FuzzyMatch