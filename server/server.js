const express = require('express');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');

const app = express();
const port = 3000;

mongoose.connect('mongodb://127.0.0.1:27017/eventsapp', {
    useNewUrlParser: true,
    useUnifiedTopology: true
}).catch(error => console.error(error));

// ===== SCHEMAS =====

const userSchema = new mongoose.Schema({
    username: { type: String, unique: true, required: true },
    password: { type: String, required: true },
    email: { type: String, required: true },
    isAdmin: { type: Boolean, required: true, default: false }
});
const User = mongoose.model('User', userSchema);

const eventSchema = new mongoose.Schema({
    name: { type: String, required: true },
    description: { type: String, default: '' },
    location: { type: String, required: true },
    eventTime: { type: String, required: true },
    category: { type: String, required: true },
    promoted: { type: Boolean, default: false },
    capacity: { type: Number, default: 0 },
    numberOfAttendees: { type: Number, default: 0 },
    avgRating: { type: Number, default: 0 },
    numberOfRatings: { type: Number, default: 0 },
    isExclusive: {type: Boolean,default:false},
    expirationTime: {type:String,default: ''}
});
const Event = mongoose.model('Event', eventSchema);

const attendanceSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    eventId: { type: mongoose.Schema.Types.ObjectId, ref: 'Event' },
    commitment: { type: String, enum: ['ZAINTERESOVAN', 'PRISUSTVUJE'], required: true }
});
attendanceSchema.index({ userId: 1, eventId: 1 }, { unique: true });
const Attendance = mongoose.model('Attendance', attendanceSchema);

const ratingSchema = new mongoose.Schema({
    userId: { type: String, required: true },
    eventId: { type: String, required: true },
    rating: { type: Number, min: 1, max: 5, required: true }
});
ratingSchema.index({ userId: 1, eventId: 1 }, { unique: true });
const Rating = mongoose.model('Rating', ratingSchema);

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

// ===== USERS =====

/**
 * @api {post} /users Create User
 * @apiName CreateUser
 * @apiGroup User
 *
 * @apiParam {String} username User's unique username.
 * @apiParam {String} password User's password.
 * @apiParam {String} email User's email.
 * @apiParam {Boolean} isAdmin Whether user is admin or not.
 *
 * @apiSuccess {Object} user Created user object.
 * @apiSuccess {String} user._id Unique id — store in local database.
 * @apiSuccess {String} user.username User's username.
 * @apiSuccess {String} user.email User's email.
 * @apiSuccess {Boolean} user.isAdmin Whether user is admin.
 * @apiError {String} message Error message.
 */
app.post('/users', async (req, res) => {
    try {
        const { username, password, email, isAdmin } = req.body;
        const existingUser = await User.findOne({ username });
        if (existingUser) {
            return res.status(409).json({ message: 'User already exists.' });
        }
        const user = new User({ username, password, email, isAdmin: isAdmin || false });
        await user.save();
        return res.json(user);
    } catch (err) {
        return res.status(400).json({ message: 'Missing or invalid parameters.' });
    }
});

/**
 * @api {post} /login Login
 * @apiName Login
 * @apiGroup User
 *
 * @apiParam {String} username User's username.
 * @apiParam {String} password User's password.
 *
 * @apiSuccess {Object} user User object.
 * @apiSuccess {String} user._id User's unique id.
 * @apiSuccess {String} user.username User's username.
 * @apiSuccess {String} user.email User's email.
 * @apiSuccess {Boolean} user.isAdmin Whether user is admin.
 * @apiError {String} message Error message.
 */
app.post('/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        const user = await User.findOne({ username, password });
        if (!user) {
            return res.status(401).json({ message: 'Invalid username or password.' });
        }
        return res.json(user);
    } catch (err) {
        return res.status(400).json({ message: 'Missing or invalid parameters.' });
    }
});

/**
 * @api {put} /password Change Password
 * @apiName ChangePassword
 * @apiGroup User
 *
 * @apiParam {String} username User's username.
 * @apiParam {String} oldPassword Old password.
 * @apiParam {String} newPassword New password.
 *
 * @apiSuccess {Object} user Updated user object.
 * @apiError {String} message Error message.
 */
app.put('/password', async (req, res) => {
    try {
        const { username, oldPassword, newPassword } = req.body;
        const user = await User.findOneAndUpdate(
            { username, password: oldPassword },
            { password: newPassword },
            { new: true }
        );
        if (!user) {
            return res.status(401).json({ message: 'Invalid username or password.' });
        }
        return res.json(user);
    } catch (err) {
        return res.status(400).json({ message: 'Missing or invalid parameters.' });
    }
});

// ===== EVENTS =====

/**
 * @api {post} /events Create Event
 * @apiName CreateEvent
 * @apiGroup Event
 *
 * @apiParam {String} name Event's name.
 * @apiParam {String} description Event's description.
 * @apiParam {String} location Event's location.
 * @apiParam {String} eventTime Date and time (dd.MM.yyyy HH:mm).
 * @apiParam {String} category Category (Party, Festival, Stand-Up & Theater, Concert, Exhibition).
 * @apiParam {Boolean} promoted Whether event is promoted.
 * @apiParam {Number} capacity Event capacity (required if promoted).
 *
 * @apiSuccess {Object} event Created event object.
 * @apiSuccess {String} event._id Unique id — store in local database.
 * @apiError {String} message Error message.
 */
app.post('/events', async (req, res) => {
    try {
        const { name, description, location, eventTime, category, promoted, capacity } = req.body;
        const event = new Event({
            name, description, location, eventTime, category,
            promoted: promoted || false,
            capacity: capacity || 0,
            isExclusive: isExclusive || false,
            expirationTime: expirationTime || ''
        });
        await event.save();
        return res.json(event);
    } catch (err) {
        return res.status(400).json({ message: 'Missing or invalid parameters.' });
    }
});

/**
 * @api {get} /events Get All Events
 * @apiName GetEvents
 * @apiGroup Event
 *
 * @apiSuccess {Object[]} events List of all events.
 * @apiError {String} message Error message.
 */
app.get('/events', async (req, res) => {
    try {
        const events = await Event.find();
        return res.json(events);
    } catch (err) {
        return res.status(500).json({ message: 'Server error.' });
    }
});

/**
 * @api {get} /events/:category Get Events by Category
 * @apiName GetEventsByCategory
 * @apiGroup Event
 *
 * @apiParam {String} category Category name.
 *
 * @apiSuccess {Object[]} events List of events for the given category.
 * @apiError {String} message Error message.
 */
app.get('/events/:category', async (req, res) => {
    try {
        const { category } = req.params;
        const events = await Event.find({ category });
        return res.json(events);
    } catch (err) {
        return res.status(500).json({ message: 'Server error.' });
    }
});

// ===== ATTENDANCE =====

/**
 * @api {post} /attendance Mark Attendance
 * @apiName MarkAttendance
 * @apiGroup Attendance
 *
 * @apiParam {String} userId User's server id (_id).
 * @apiParam {String} eventId Event's server id (_id).
 * @apiParam {String} commitment ZAINTERESOVAN or PRISUSTVUJE.
 *
 * @apiSuccess {Object} attendance Attendance record.
 * @apiError {String} message Error message.
 */
app.post('/attendance', async (req, res) => {
    try {
        const { userId, eventId, commitment } = req.body;

        if (commitment === 'PRISUSTVUJE') {
            const event = await Event.findById(eventId);
            if (event && event.promoted && event.numberOfAttendees >= event.capacity) {
                return res.status(400).json({ message: 'No available spots for this event.' });
            }
        }

        const existing = await Attendance.findOne({ userId, eventId });
        if (existing) {
            const wasAttending = existing.commitment === 'PRISUSTVUJE';
            existing.commitment = commitment;
            await existing.save();
            if (!wasAttending && commitment === 'PRISUSTVUJE') {
                await Event.findByIdAndUpdate(eventId, { $inc: { numberOfAttendees: 1 } });
            }
            return res.json(existing);
        }

        const attendance = new Attendance({ userId, eventId, commitment });
        await attendance.save();
        if (commitment === 'PRISUSTVUJE') {
            await Event.findByIdAndUpdate(eventId, { $inc: { numberOfAttendees: 1 } });
        }
        return res.json(attendance);
    } catch (err) {
        return res.status(400).json({ message: 'Missing or invalid parameters.' });
    }
});

/**
 * @api {get} /attendance/:userId Get Attendance for User
 * @apiName GetAttendance
 * @apiGroup Attendance
 *
 * @apiParam {String} userId User's server id (_id).
 *
 * @apiSuccess {Object[]} attendance List of attendance records.
 * @apiError {String} message Error message.
 */
app.get('/attendance/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const attendance = await Attendance.find({ userId });
        return res.json(attendance);
    } catch (err) {
        return res.status(500).json({ message: 'Server error.' });
    }
});

// ===== RATINGS =====

/**
 * @api {post} /ratings Submit Rating
 * @apiName SubmitRating
 * @apiGroup Rating
 *
 * @apiParam {String} userId User's server id (_id).
 * @apiParam {String} eventId Event's server id (_id).
 * @apiParam {Number} rating Rating value (1–5).
 *
 * @apiSuccess {Object} event Updated event object with new avgRating and numberOfRatings.
 * @apiError {String} message Error message.
 */
app.post('/ratings', async (req, res) => {
    try {
        const { userId, eventId, rating } = req.body;

        const existingRating = await Rating.findOne({ userId, eventId });
        if (existingRating) {
            return res.status(409).json({ message: 'User already rated this event.' });
        }

        const newRating = new Rating({ userId, eventId, rating });
        await newRating.save();

        const allRatings = await Rating.find({ eventId });
        const avgRating = allRatings.reduce((sum, r) => sum + r.rating, 0) / allRatings.length;
        const event = await Event.findByIdAndUpdate(
            eventId,
            { avgRating, numberOfRatings: allRatings.length },
            { new: true }
        );

        return res.json(event);
    } catch (err) {
        return res.status(400).json({ message: 'Missing or invalid parameters.' });
    }
});

/**
 * @api {get} /friends-activity/:userId Get Friends Activity
 * @apiName GetFriendsActivity
 * @apiGroup Attendance
 *
 * @apiParam {String} userId User's server id (_id).
 *
 * @apiSuccess {Object[]} activity List of other users' attendance records for events the user also attends.
 * @apiSuccess {String} activity.username Username of the other attendee.
 * @apiSuccess {String} activity.eventName Name of the shared event.
 * @apiSuccess {String} activity.commitment Other user's commitment (ZAINTERESOVAN or PRISUSTVUJE).
 * @apiError {String} error Error message.
 */
app.get('/friends-activity/:userId', async (req, res) => {
    try {
        const myAttendance = await Attendance.find({ userId: req.params.userId });
        const myEventIds = myAttendance.map(a => a.eventId);

        const others = await Attendance.find({
            eventId: { $in: myEventIds },
            userId: { $ne: req.params.userId }
        }).populate('userId', 'username').populate('eventId', 'name');

        const result = others.map(a => ({
            username: a.userId ? a.userId.username : 'Unknown',
            eventName: a.eventId ? a.eventId.name : 'Unknown',
            commitment: a.commitment
        }));

        res.json(result);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

/**
 * @api {get} /attendance/:userId Get Attendance with Event Details
 * @apiName GetAttendancePopulated
 * @apiGroup Attendance
 *
 * @apiParam {String} userId User's server id (_id).
 *
 * @apiSuccess {Object[]} attendance List of attendance records with full event objects.
 * @apiSuccess {String} attendance.commitment User's commitment (ZAINTERESOVAN or PRISUSTVUJE).
 * @apiSuccess {Object} attendance.event Full event object.
 * @apiError {String} error Error message.
 */
app.get('/attendance/:userId', async (req, res) => {
    try {
        const attendance = await Attendance.find({ userId: req.params.userId })
            .populate('eventId');

        const result = attendance.map(a => ({
            commitment: a.commitment,
            event: a.eventId
        }));

        res.json(result);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});


app.listen(port, () => {
    console.log(`Server started on port ${port}`);
});

