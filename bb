if not math.clamp then
    math.clamp = function(val, min, max)
        return math.min(math.max(val, min), max)
    end
end

local Players = game:GetService("Players")
local UserInputService = game:GetService("UserInputService")
local RunService = game:GetService("RunService")
local HttpService = game:GetService("HttpService")

local LocalPlayer = Players.LocalPlayer
local LocalMouse = LocalPlayer and LocalPlayer:GetMouse() or nil

local Theme = {
    bg = Color3.fromRGB(22, 23, 28),
    shell = Color3.fromRGB(27, 29, 35),
    sidebar = Color3.fromRGB(31, 33, 39),
    panel = Color3.fromRGB(31, 33, 39),
    panelAlt = Color3.fromRGB(39, 41, 49),
    card = Color3.fromRGB(37, 39, 47),
    border = Color3.fromRGB(47, 50, 58),
    borderSoft = Color3.fromRGB(40, 42, 49),
    topHighlight = Color3.fromRGB(59, 62, 72),
    cardInner = Color3.fromRGB(43, 45, 54),
    accent = Color3.fromRGB(54, 248, 87),
    accentSoft = Color3.fromRGB(40, 180, 70),
    text = Color3.fromRGB(242, 243, 246),
    textDim = Color3.fromRGB(161, 165, 174),
    textMuted = Color3.fromRGB(128, 132, 141),
    success = Color3.fromRGB(255, 255, 255),
}

local AccentThemes = {
    ["Green"] = {
        accent = Color3.fromRGB(54, 248, 87),
        accentSoft = Color3.fromRGB(40, 180, 70),
    },
    ["Blue"] = {
        accent = Color3.fromRGB(70, 170, 255),
        accentSoft = Color3.fromRGB(50, 120, 195),
    },
    ["Red"] = {
        accent = Color3.fromRGB(255, 92, 92),
        accentSoft = Color3.fromRGB(190, 68, 68),
    },
    ["Orange"] = {
        accent = Color3.fromRGB(255, 164, 58),
        accentSoft = Color3.fromRGB(195, 116, 38),
    },
    ["Pink"] = {
        accent = Color3.fromRGB(255, 105, 180),
        accentSoft = Color3.fromRGB(196, 73, 136),
    },
    ["White"] = {
        accent = Color3.fromRGB(235, 235, 235),
        accentSoft = Color3.fromRGB(155, 155, 155),
    },
    ["Black"] = {
        accent = Color3.fromRGB(18, 18, 18),
        accentSoft = Color3.fromRGB(48, 48, 48),
    },
    ["Rainbow"] = {
        accent = Color3.fromRGB(54, 248, 87),
        accentSoft = Color3.fromRGB(40, 180, 70),
    },
}

local function NewBucket()
    return {
        objects = {},
        visible = false,
        built = false,
        deferVisible = false,
    }
end

local UI = {
    isRunning = true,
    isBooting = true,
    bootReady = false,
    bootStartedAt = tick(),
    bootDuration = 0.1,
    bootPanelScale = 0.4,
    bootVisualProgress = 0,
    bootVisualTarget = 0.06,
    uiVisible = true,
    uiAlpha = 1,
    uiOffset = 0,
    renderX = nil,
    renderY = nil,
    renderScale = 1,
    contentAlpha = 1,
    contentOffset = 0,
    activeTabHighlightY = 0,
    animatedScrollThumbY = nil,
    needsRedraw = true,
    shellDirty = true,
    tabDirty = {true, true, true, true, true, true},
    hitboxes = {},
    hoveredId = nil,
    lastMousePressed = false,
    draggingWindow = false,
    draggingScroll = false,
    draggingSlider = false,
    activeSlider = nil,
    dragOffsetX = 0,
    dragOffsetY = 0,
    scrollDragOffset = 0,
    lastToggleTick = 0,
    launchTick = tick(),
    lastHomeStatRefresh = 0,
    lastConfigSaveTick = nil,
    tweenSpeedWarned = false,
    carFlyEnabled = false,
    carFlyActive = false,
    carFlySpeed = 80,
    carFlyPosition = nil,
    carFlyTargetPosition = nil,
    lastCarFlyTick = 0,
    lastCarFlyToggleTick = 0,
    activeTweenId = 0,
    tweenNoclipActive = false,
    tweenNoclipStates = {},
    tweenGravityActive = false,
    minUpdateDelta = 1 / 60,
    lastFrameTick = 0,
    lastEspUpdateTick = 0,
    tweenState = {
        active = false,
        stages = nil,
        tweenId = 0,
        stageIndex = 0,
        stageStart = nil,
        stageTarget = nil,
        stageDuration = 0,
        stageStartedAt = 0,
    },
    farmWorkerRunning = false,
    farmState = {
        active = false,
        lastFarmNotifyAt = 0,
    },
    pathNodes = {},
    pathDebugDrawings = nil,
    expandedDropdown = nil,
    selectedTab = 1,
    settingsTabIndex = 4,
    debugTabIndex = 5,
    premiumTabIndex = 6,
    configFolderName = "WindyBloxburg",
    configFileName = "config.json",
    logoImageUrl = "https://static.wikia.nocookie.net/bee-swarm-simulator/images/8/85/Windy_Bee.png/revision/latest?cb=20230415210459",
    logoImageData = nil,
    logoImageFailed = false,
    logoImageUnsupported = false,
    animationState = {
        tabHover = {},
        tabActive = {},
        controlHover = {},
        dropdownOpen = {},
        sliderFill = {},
        toggleFill = {},
        checkboxFill = {},
    },

    window = {
        x = 220,
        y = 140,
        width = 620,
        height = 352,
        headerHeight = 42,
        footerHeight = 20,
        padding = 16,
        sidebarWidth = 185,
    },

    scroll = {
        offset = 0,
        max = 0,
    },

    shellBucket = NewBucket(),
    loadingBucket = NewBucket(),
    espObjects = {},
    activeEspKeys = {
        debug = {},
    },
    tabBuckets = {
        NewBucket(),
        NewBucket(),
        NewBucket(),
        NewBucket(),
        NewBucket(),
        NewBucket(),
    },

    tabs = {
        {name = "HOME"},
        {name = "FARM"},
        {name = "TELEPORTS"},
        {name = "SETTINGS"},
        {name = "DEBUG"},
        {name = "PREMIUM"},
    },

    controls = {
        {
            {id = "uptime_stat", type = "stat", label = "Uptime", value = "0s"},
            {id = "profile_stat", type = "stat", label = "Status", value = "Undetected"},
            {id = "farm_state_stat", type = "stat", label = "Farm State", value = "Idle"},
            {id = "on_car_stat", type = "stat", label = "On Car", value = "False"},
            {id = "last_save_stat", type = "stat", label = "Last Config Save", value = "Never"},
            {id = "panic_reset_button", type = "button", label = "Panic Reset", description = "", accent = "accent"},
        },
        {
            {id = "farm_toggle", type = "toggle", label = "Farm", description = "", value = false},
            {id = "goto_job_button", type = "button", label = "Goto Job", description = "", accent = "accent"},
            {id = "job_selection", type = "dropdown_single", label = "Job Selection", description = "", value = "Select Job", options = {"Select Job", "Pizza Delivery Job", "Bloxburg Taxi", "BFF Supermarket", "Lumber Jack", "Miner", "Janitor"}},
        },
        {
            {id = "player_tp", type = "dropdown_single", label = "Player TP", description = "", value = "Select Player", options = {"Select Player"}},
            {id = "city_tp", type = "dropdown_single", label = "City Teleports", description = "", value = "Select Location", options = {"Select Location", "City Center", "City Hall", "Pizza Planet", "Blox Burgers", "BFF Supermarket", "High School", "Lovely Lumber", "Ben's Ice Cream", "Mike's Motors", "The Bloxburg Cave", "Bloxburg Taxi", "Hair Studio", "Fishing Place"}},
        },
        {
            {id = "ui_scale", type = "slider", label = "UI Width", description = "", min = 540, max = 760, step = 10, value = 620},
            {id = "ui_height", type = "slider", label = "UI Height", description = "", min = 300, max = 520, step = 8, value = 352},
            {id = "tween_speed", type = "slider", label = "Tween Speed", description = "", min = 5, max = 40, step = 1, value = 20},
            {id = "accent_theme", type = "dropdown_single", label = "UI Color", description = "", value = "Green", options = {"Green", "Blue", "Red", "Orange", "Pink", "White", "Black", "Rainbow"}},
            {id = "ui_animations", type = "checkbox", label = "Animations", description = "", value = true},
            {id = "auto_save_config", type = "toggle", label = "Auto Save Config", description = "", value = true},
            {id = "discord_button", type = "button", label = "Discord", description = "", accent = "accent"},
        },
        {
            {id = "pizza_bike_esp", type = "toggle", label = "Pizza Bike Esp", description = "", value = false},
            {id = "path_esp", type = "toggle", label = "Path ESP", description = "", value = false},
            {id = "node_esp", type = "toggle", label = "Node ESP", description = "", value = false},
        },
        {
            {id = "car_fly_toggle", type = "toggle", label = "Car Fly", description = "", value = false},
            {id = "car_fly_speed", type = "slider", label = "Car Fly Speed", description = "", min = 20, max = 140, step = 5, value = 80},
            {id = "car_speed_toggle", type = "toggle", label = "Car Speed", description = "", value = false},
            {id = "car_speed_multiplier", type = "slider", label = "Car Speed Multiplier", description = "", min = 0.1, max = 3, step = 0.1, value = 1.6},
        },
    },
}

local function LerpNumber(a, b, t)
    return a + ((b - a) * t)
end

local function LerpColor(a, b, t)
    return Color3.new(
        LerpNumber(a.R, b.R, t),
        LerpNumber(a.G, b.G, t),
        LerpNumber(a.B, b.B, t)
    )
end

local function SmoothFactor(speed, dt)
    return 1 - math.exp(-(speed or 1) * (dt or (1 / 60)))
end

local function NearlyEqual(a, b, epsilon)
    return math.abs((a or 0) - (b or 0)) <= (epsilon or 0.001)
end

local VK_Z = 0x5A

local function TweenDebug(message)
end

local function FarmDebug(message)
end

local function EspDebug(message)
    print("[Bloxburg] " .. tostring(message))
end

local function TaxiDebug(message)
    print("[Bloxburg] " .. tostring(message))
end

local function JobDebug(message)
    print("[Bloxburg] " .. tostring(message))
end

local CityTeleportTargets = {
    ["City Center"] = Vector3.new(360, 7, -172),
    ["City Hall"] = Vector3.new(599, 20, -8),
    ["Pizza Planet"] = Vector3.new(-65, 5, -47),
    ["Blox Burgers"] = Vector3.new(215, 5, 292),
    ["BFF Supermarket"] = Vector3.new(548, 23, -722),
    ["High School"] = Vector3.new(1000, 47, -249),
    ["Lovely Lumber"] = Vector3.new(3113, 140, -1834),
    ["Ben's Ice Cream"] = Vector3.new(-254, 5, -239),
    ["Mike's Motors"] = Vector3.new(288, 4, -291),
    ["The Bloxburg Cave"] = Vector3.new(2130, 133, -2453),
    ["Bloxburg Taxi"] = Vector3.new(119, 4, 75),
    ["Hair Studio"] = Vector3.new(-51, 5, -331),
    ["Fishing Place"] = Vector3.new(-270, 5, -104),
}

local PIZZA_PICKUP_POSITION = Vector3.new(-48, 7, -42)
local TAXI_JOB_POSITION = Vector3.new(119, 4, 75)
local BFF_JOB_POSITION = Vector3.new(554, 23, -785)
local BFF_STORAGE_ENTRANCE_DOOR = Vector3.new(539, 23, -783)
local BFF_BACK_SHELF_START = Vector3.new(528, 23, -791)
local BFF_BACK_SHELF_END = Vector3.new(487, 23, -791)
local BFF_BACK_RIGHT_START = Vector3.new(485, 23, -777)
local BFF_BACK_RIGHT_END = Vector3.new(485, 23, -733)
local BFF_RM_START = Vector3.new(502, 23, -746)
local BFF_RM_END = Vector3.new(501, 23, -773)
local BFF_LM_START = Vector3.new(514, 23, -772)
local BFF_LM_END = Vector3.new(514, 23, -746)
local BFF_APPLE = Vector3.new(530, 26, -759)
local BFF_MIDDLE_BACK_START = Vector3.new(530, 23, -746)
local BFF_MIDDLE_BACK_END = Vector3.new(555, 23, -745)
local BFF_APPLE_LAST = Vector3.new(561, 26, -739)
local BFF_MIDDLE_FRONT_START = Vector3.new(552, 23, -733)
local BFF_MIDDLE_FRONT_END = Vector3.new(524, 23, -733)
local BFF_SINGLE_SHELF_START = Vector3.new(558, 23, -724)
local BFF_SINGLE_SHELF_END = Vector3.new(558, 23, -707)
local BFF_RIGHT_SHELF_START = Vector3.new(578, 23, -708)
local BFF_RIGHT_SHELF_END = Vector3.new(579, 23, -751)
local BFF_FREEZER_START = Vector3.new(579, 23, -764)
local BFF_FREEZER_END = Vector3.new(547, 23, -764)
local BFF_CRATE_AREA_MIN = Vector3.new(540, 30, -796)
local BFF_CRATE_AREA_MAX = Vector3.new(583, 33, -771)

local TWEEN_SPEED_SCALE = 0.85
local TWEEN_MIN_STAGE_DURATION = 0.1

local BFF_PATH = {
    {name = "storage_entrance_door", position = BFF_STORAGE_ENTRANCE_DOOR},
    {name = "back_shelf_start", position = BFF_BACK_SHELF_START},
    {name = "back_shelf_end", position = BFF_BACK_SHELF_END},
    {name = "back_right_start", position = BFF_BACK_RIGHT_START},
    {name = "back_right_end", position = BFF_BACK_RIGHT_END},
    {name = "rm_start", position = BFF_RM_START},
    {name = "rm_end", position = BFF_RM_END},
    {name = "lm_start", position = BFF_LM_START},
    {name = "lm_end", position = BFF_LM_END},
    {name = "apple", position = BFF_APPLE},
    {name = "middle_back_start", position = BFF_MIDDLE_BACK_START},
    {name = "middle_back_end", position = BFF_MIDDLE_BACK_END},
    {name = "apple_last", position = BFF_APPLE_LAST},
    {name = "middle_front_start", position = BFF_MIDDLE_FRONT_START},
    {name = "middle_front_end", position = BFF_MIDDLE_FRONT_END},
    {name = "single_shelf_start", position = BFF_SINGLE_SHELF_START},
    {name = "single_shelf_end", position = BFF_SINGLE_SHELF_END},
    {name = "right_shelf_start", position = BFF_RIGHT_SHELF_START},
    {name = "right_shelf_end", position = BFF_RIGHT_SHELF_END},
    {name = "freezer_start", position = BFF_FREEZER_START},
    {name = "freezer_end", position = BFF_FREEZER_END},
}

local BFF_RETURN_PATH = {
    {name = "middle_back_start_return", position = BFF_MIDDLE_BACK_START},
    {name = "lm_start_return", position = BFF_LM_START},
    {name = "storage_entrance_door_return", position = BFF_STORAGE_ENTRANCE_DOOR},
    {name = "job_start_return", position = BFF_JOB_POSITION},
}

local PREMIUM_JOBS = {
    ["Lumber Jack"] = true,
    ["Miner"] = true,
    ["Janitor"] = true,
}

local PREMIUM_USERS = {
    ["Ii2to2w"] = true,
}

function UI:Track(bucket, obj)
    if bucket and bucket.deferVisible then
        obj.Visible = false
    end
    table.insert(bucket.objects, obj)
    return obj
end

function UI:GetRenderTransparency(value)
    return math.clamp((value or 1) * (self.renderAlpha or 1), 0, 1)
end

function UI:SetDrawingProperty(obj, property, value, epsilon)
    local current = obj[property]
    if type(current) == "number" and type(value) == "number" then
        if NearlyEqual(current, value, epsilon) then
            return false
        end
    elseif current == value then
        return false
    end

    obj[property] = value
    return true
end

function UI:IsShellHitboxId(id)
    return type(id) == "string" and string.sub(id, 1, 4) == "tab_"
end

function UI:ClearBucket(bucket)
    for _, obj in ipairs(bucket.objects) do
        pcall(function()
            obj:Remove()
        end)
    end
    bucket.objects = {}
    bucket.visible = false
    bucket.built = false
end

function UI:HideBucket(bucket)
    for _, obj in ipairs(bucket.objects) do
        obj.Visible = false
    end
    bucket.visible = false
end

function UI:ShowBucket(bucket)
    for _, obj in ipairs(bucket.objects) do
        obj.Visible = true
    end
    bucket.visible = true
end

function UI:CreateSquare(bucket, x, y, w, h, color, transparency, filled, thickness)
    local square = Drawing.new("Square")
    local tx, ty = self:TransformPoint(x, y)
    local tw, th = self:TransformSize(w, h)
    self:SetDrawingProperty(square, "Position", Vector2.new(tx, ty))
    self:SetDrawingProperty(square, "Size", Vector2.new(tw, th))
    self:SetDrawingProperty(square, "Color", color)
    self:SetDrawingProperty(square, "Transparency", self:GetRenderTransparency(transparency or 1), 0.001)
    self:SetDrawingProperty(square, "Filled", filled ~= false)
    self:SetDrawingProperty(square, "Visible", not (bucket and bucket.deferVisible))
    if not square.Filled then
        self:SetDrawingProperty(square, "Thickness", thickness or 1, 0.001)
    end
    return self:Track(bucket, square)
end

function UI:CreateText(bucket, text, x, y, color, size, center)
    local label = Drawing.new("Text")
    local tx, ty = self:TransformPoint(x, y)

    self:SetDrawingProperty(label, "Text", text)
    self:SetDrawingProperty(label, "Position", Vector2.new(tx, ty))
    self:SetDrawingProperty(label, "Color", color)
    self:SetDrawingProperty(label, "Transparency", 1, 0.001)
    self:SetDrawingProperty(label, "Size", math.max(8, math.floor((size or 13) * (self.renderScale or 1) + 0.5)), 0.001)
    self:SetDrawingProperty(label, "Center", center or false)
    self:SetDrawingProperty(label, "Outline", true)
    self:SetDrawingProperty(label, "Visible", not (bucket and bucket.deferVisible))
    self:SetDrawingProperty(label, "Font", 2)
    return self:Track(bucket, label)
end

function UI:CreateRightText(bucket, text, rightX, y, color, size)
    local content = tostring(text or "")
    local fontSize = size or 13
    local estimatedWidth = math.max(0, math.floor(#content * (fontSize * 0.52)))
    return self:CreateText(bucket, content, rightX - estimatedWidth, y, color, fontSize, false)
end

function UI:CreateLine(bucket, fromX, fromY, toX, toY, color, transparency, thickness)
    local line = Drawing.new("Line")
    local tx1, ty1 = self:TransformPoint(fromX, fromY)
    local tx2, ty2 = self:TransformPoint(toX, toY)
    self:SetDrawingProperty(line, "From", Vector2.new(tx1, ty1))
    self:SetDrawingProperty(line, "To", Vector2.new(tx2, ty2))
    self:SetDrawingProperty(line, "Color", color)
    self:SetDrawingProperty(line, "Transparency", self:GetRenderTransparency(transparency or 1), 0.001)
    self:SetDrawingProperty(line, "Thickness", math.max(1, math.floor((thickness or 1) * (self.renderScale or 1) + 0.5)), 0.001)
    self:SetDrawingProperty(line, "Visible", not (bucket and bucket.deferVisible))
    return self:Track(bucket, line)
end

function UI:CreateCircle(bucket, x, y, radius, color, transparency, filled, thickness)
    local circle = Drawing.new("Circle")
    local tx, ty = self:TransformPoint(x, y)
    self:SetDrawingProperty(circle, "Position", Vector2.new(tx, ty))
    self:SetDrawingProperty(circle, "Radius", math.max(1, radius * (self.renderScale or 1)), 0.001)
    self:SetDrawingProperty(circle, "Color", color)
    self:SetDrawingProperty(circle, "Transparency", self:GetRenderTransparency(transparency or 1), 0.001)
    self:SetDrawingProperty(circle, "Filled", filled ~= false)
    self:SetDrawingProperty(circle, "Visible", not (bucket and bucket.deferVisible))
    if not circle.Filled then
        self:SetDrawingProperty(circle, "Thickness", math.max(1, math.floor((thickness or 1) * (self.renderScale or 1) + 0.5)), 0.001)
    end
    return self:Track(bucket, circle)
end

function UI:GetLogoImageData()
    if self.logoImageUnsupported then
        return nil
    end

    if self.logoImageData or self.logoImageFailed then
        return self.logoImageData
    end

    local ok, data = pcall(function()
        return game:HttpGet(self.logoImageUrl)
    end)

    if ok and type(data) == "string" and data ~= "" then
        self.logoImageData = data
        return self.logoImageData
    end

    self.logoImageFailed = true
    return nil
end

function UI:IsLogoImageReady()
    return self.logoImageData ~= nil or self.logoImageFailed == true or self.logoImageUnsupported == true
end

function UI:CreateImage(bucket, data, x, y, w, h, transparency)
    if self.logoImageUnsupported then
        return nil
    end

    local ok, image = pcall(Drawing.new, "Image")
    if not ok or not image then
        self.logoImageUnsupported = true
        return nil
    end

    local tx, ty = self:TransformPoint(x, y)
    local tw, th = self:TransformSize(w, h)
    local applied = pcall(function()
        self:SetDrawingProperty(image, "Position", Vector2.new(tx, ty))
        self:SetDrawingProperty(image, "Size", Vector2.new(tw, th))
        self:SetDrawingProperty(image, "Visible", not (bucket and bucket.deferVisible))
        self:SetDrawingProperty(image, "Transparency", self:GetRenderTransparency(transparency or 1), 0.001)
        self:SetDrawingProperty(image, "Data", data)
    end)

    if not applied then
        self.logoImageUnsupported = true
        pcall(function()
            image:Remove()
        end)
        return nil
    end

    return self:Track(bucket, image)
end

function UI:CreateRoundedBox(bucket, x, y, w, h, radius, color)
    radius = math.floor(math.max(0, math.min(radius or 0, math.min(w, h) / 2)))
    if radius <= 0 then
        self:CreateSquare(bucket, x, y, w, h, color, 1, true)
        return
    end

    self:CreateSquare(bucket, x + radius, y, w - (radius * 2), h, color, 1, true)
    self:CreateSquare(bucket, x, y + radius, w, h - (radius * 2), color, 1, true)
    self:CreateCircle(bucket, x + radius, y + radius, radius, color, 1, true)
    self:CreateCircle(bucket, x + w - radius, y + radius, radius, color, 1, true)
    self:CreateCircle(bucket, x + radius, y + h - radius, radius, color, 1, true)
    self:CreateCircle(bucket, x + w - radius, y + h - radius, radius, color, 1, true)
end

function UI:AnimateValue(store, key, target, speed, dt, defaultValue)
    local current = store[key]
    if current == nil then
        current = defaultValue ~= nil and defaultValue or target
    end

    local nextValue = LerpNumber(current, target, SmoothFactor(speed or 12, dt))
    if math.abs(nextValue - target) < 0.001 then
        nextValue = target
    end

    store[key] = nextValue
    return nextValue, nextValue ~= target
end

function UI:GetAnimatedWindowYOffset()
    return math.floor(self.uiOffset + 0.5)
end

function UI:GetViewportSize()
    local camera = workspace and workspace.CurrentCamera or nil
    if camera and camera.ViewportSize then
        return camera.ViewportSize
    end

    return Vector2.new(1280, 720)
end

function UI:GetWorldToScreen(position)
    if typeof(position) ~= "Vector3" then
        return nil, false
    end

    if type(WorldToScreen) == "function" then
        local ok, screenPos, onScreen = pcall(WorldToScreen, position)
        if ok and screenPos and onScreen == true then
            return screenPos, onScreen == true
        end
    end

    local camera = workspace and workspace.CurrentCamera or nil
    if camera and camera.WorldToViewportPoint then
        local ok, vector, visible = pcall(function()
            return camera:WorldToViewportPoint(position)
        end)
        if ok and vector then
            return Vector2.new(vector.X, vector.Y), visible == true
        end
    end

    return nil, false
end

local WALKTOPOINT_OFFSET_1 = 0x16C
local DIRECTION_OFFSET = 0x170
local WALKTOPOINT_OFFSET_2 = 0x17C
local WALK_TARGET_THRESHOLD = 0.5

local WalkToService = {
    isWalking = false,
    currentTarget = nil,
    humanoid = nil,
    character = nil,
    updateThread = nil,
}

local function WriteVector3(address, offset, vector3)
    if not address or typeof(vector3) ~= "Vector3" or type(memory_write) ~= "function" then
        return false
    end

    local ok = pcall(function()
        memory_write("float", address + offset, vector3.X)
        memory_write("float", address + offset + 4, vector3.Y)
        memory_write("float", address + offset + 8, vector3.Z)
    end)

    return ok
end

local function GetPartPosition(part)
    if not part then
        return nil, nil, nil
    end

    local pos = part.Position
    if typeof(pos) == "Vector3" then
        return pos.X, pos.Y, pos.Z
    end

    return nil, nil, nil
end

function WalkToService.WalkTo(humanoid, targetPosition)
    if not humanoid or not humanoid.Address or typeof(targetPosition) ~= "Vector3" then
        return false
    end

    local character = humanoid.Parent
    local hrp = character and character:FindFirstChild("HumanoidRootPart")
    if not hrp then
        return false
    end

    WalkToService.humanoid = humanoid
    WalkToService.character = character
    WalkToService.currentTarget = targetPosition
    WalkToService.isWalking = true

    local posX, posY, posZ = GetPartPosition(hrp)
    if not posX then
        return false
    end

    local dx = targetPosition.X - posX
    local dy = targetPosition.Y - posY
    local dz = targetPosition.Z - posZ
    local distance = math.sqrt(dx * dx + dy * dy + dz * dz)
    if distance <= 0 then
        return false
    end

    local direction = Vector3.new(dx / distance, dy / distance, dz / distance)
    local humAddress = humanoid.Address

    WriteVector3(humAddress, WALKTOPOINT_OFFSET_1, targetPosition)
    WriteVector3(humAddress, DIRECTION_OFFSET, direction)
    WriteVector3(humAddress, WALKTOPOINT_OFFSET_2, targetPosition)

    WalkToService.updateThread = task.spawn(function()
        while WalkToService.isWalking do
            local liveHrp = WalkToService.character and WalkToService.character:FindFirstChild("HumanoidRootPart")
            if not liveHrp then
                WalkToService.Stop()
                break
            end

            local liveX, liveY, liveZ = GetPartPosition(liveHrp)
            if not liveX then
                WalkToService.Stop()
                break
            end

            local target = WalkToService.currentTarget
            if typeof(target) ~= "Vector3" then
                WalkToService.Stop()
                break
            end

            local ddx = target.X - liveX
            local ddy = target.Y - liveY
            local ddz = target.Z - liveZ
            local liveDistance = math.sqrt(ddx * ddx + ddy * ddy + ddz * ddz)
            if liveDistance <= WALK_TARGET_THRESHOLD then
                WalkToService.Stop()
                break
            end

            local liveDirection = Vector3.new(ddx / liveDistance, ddy / liveDistance, ddz / liveDistance)
            WriteVector3(humAddress, WALKTOPOINT_OFFSET_1, target)
            WriteVector3(humAddress, DIRECTION_OFFSET, liveDirection)
            WriteVector3(humAddress, WALKTOPOINT_OFFSET_2, target)
            task.wait(0.1)
        end
    end)

    return true
end

function WalkToService.Stop()
    WalkToService.isWalking = false

    if WalkToService.humanoid and WalkToService.humanoid.Address then
        local zero = Vector3.new(0, 0, 0)
        local humAddress = WalkToService.humanoid.Address
        WriteVector3(humAddress, WALKTOPOINT_OFFSET_1, zero)
        WriteVector3(humAddress, DIRECTION_OFFSET, zero)
        WriteVector3(humAddress, WALKTOPOINT_OFFSET_2, zero)
    end

    WalkToService.currentTarget = nil
    WalkToService.updateThread = nil
end

function UI:WalkTo(targetPosition)
    local character = LocalPlayer and LocalPlayer.Character
    local humanoid = character and character:FindFirstChild("Humanoid")
    if not humanoid then
        return false
    end

    return WalkToService.WalkTo(humanoid, targetPosition)
end

function UI:StopWalking()
    WalkToService.Stop()
end

function UI:IsWalking()
    return WalkToService.isWalking == true
end

function UI:SetPathNodes(nodes)
    self.pathNodes = {}
    for index, node in ipairs(nodes or {}) do
        if typeof(node) == "Vector3" then
            self.pathNodes[#self.pathNodes + 1] = {
                name = string.format("node_%d", index),
                position = node,
            }
        elseif type(node) == "table" and typeof(node.position) == "Vector3" then
            self.pathNodes[#self.pathNodes + 1] = {
                name = tostring(node.name or string.format("node_%d", index)),
                position = node.position,
            }
        end
    end
end

function UI:ClearPathNodes()
    self.pathNodes = {}
end

function UI:GetClosestBffReturnIndex(currentPosition, maxDistance)
    if typeof(currentPosition) ~= "Vector3" then
        return nil
    end

    local bestIndex = nil
    local bestDistance = tonumber(maxDistance) or 6

    for index, nodeEntry in ipairs(BFF_RETURN_PATH) do
        if nodeEntry and typeof(nodeEntry.position) == "Vector3" then
            local distance = (currentPosition - nodeEntry.position).Magnitude
            if distance <= bestDistance then
                bestDistance = distance
                bestIndex = index
            end
        end
    end

    return bestIndex
end

function UI:CreateEspText()
    local text = Drawing.new("Text")
    text.Center = true
    text.Outline = true
    text.Font = 2
    text.Size = 13
    text.Transparency = 1
    text.ZIndex = 0
    text.Visible = false
    return text
end

function UI:GetEspEntry(key)
    local entry = self.espObjects[key]
    if entry then
        return entry
    end

    entry = {
        box = Drawing.new("Square"),
        label = self:CreateEspText(),
    }

    entry.box.Visible = false
    entry.box.Filled = false
    entry.box.Thickness = 1
    entry.box.Transparency = 1
    entry.box.ZIndex = 0

    self.espObjects[key] = entry
    return entry
end

function UI:HideEspEntry(entry)
    if not entry then
        return
    end

    if entry.box then
        entry.box.Visible = false
    end

    if entry.label then
        entry.label.Visible = false
    end
end

function UI:HideEspPrefix(prefix)
    local wanted = tostring(prefix or "")
    for key, entry in pairs(self.espObjects) do
        if string.sub(tostring(key), 1, #wanted) == wanted then
            self:HideEspEntry(entry)
        end
    end
end

function UI:CleanupTrackedEspKeys(bucketKey, seen)
    local tracked = self.activeEspKeys[bucketKey] or {}
    for key, _ in pairs(tracked) do
        if not seen[key] then
            local entry = self.espObjects[key]
            if entry then
                self:HideEspEntry(entry)
            end
            tracked[key] = nil
        end
    end
    self.activeEspKeys[bucketKey] = tracked
end

function UI:GetRenderWindowPosition()
    return math.floor((self.renderX or self.window.x) + 0.5), math.floor((self.renderY or self.window.y) + 0.5)
end

function UI:GetTransformCenter()
    local renderX, renderY = self:GetRenderWindowPosition()
    local baseY = renderY + self:GetAnimatedWindowYOffset()
    return renderX + (self.window.width * 0.5), baseY + (self.window.height * 0.5)
end

function UI:TransformPoint(x, y)
    local scale = self.renderScale or 1
    if math.abs(scale - 1) < 0.001 then
        return x, y
    end

    local centerX, centerY = self:GetTransformCenter()
    return centerX + ((x - centerX) * scale), centerY + ((y - centerY) * scale)
end

function UI:TransformSize(w, h)
    local scale = self.renderScale or 1
    if math.abs(scale - 1) < 0.001 then
        return w, h
    end

    return w * scale, h * scale
end

function UI:ResetContentAnimation()
    self.contentAlpha = 0
    self.contentOffset = 6
    self:MarkCurrentTabDirty()
end

function UI:UpdateAnimations(dt)
    if not self:AreAnimationsEnabled() then
        self.uiAlpha = self.uiVisible and 1 or 0
        self.uiOffset = 0
        self.renderX = self.window.x
        self.renderY = self.window.y
        self.renderScale = 1
        self.contentAlpha = self.uiVisible and 1 or 0
        self.contentOffset = 0
        self.activeTabHighlightY = 110 + ((self.selectedTab - 1) * 32)

        for tabIndex = 1, #self.tabs do
            self.animationState.tabHover[tabIndex] = self.hoveredId == ("tab_" .. tostring(tabIndex)) and 1 or 0
            self.animationState.tabActive[tabIndex] = self.selectedTab == tabIndex and 1 or 0
        end

        for _, tabControls in ipairs(self.controls or {}) do
            for _, control in ipairs(tabControls) do
                self.animationState.controlHover[control.id] = self.hoveredId == control.id and 1 or 0
                self.animationState.dropdownOpen[control.id] = self.expandedDropdown == control.id and 1 or 0
                self.animationState.sliderFill[control.id] = control.type == "slider" and self:GetSliderPercent(control) or self.animationState.sliderFill[control.id]
                self.animationState.toggleFill[control.id] = control.type == "toggle" and (control.value and 1 or 0) or self.animationState.toggleFill[control.id]
                self.animationState.checkboxFill[control.id] = control.type == "checkbox" and (control.value and 1 or 0) or self.animationState.checkboxFill[control.id]
            end
        end

        local _, _, _, _, thumbTargetY = self:GetScrollbarMetrics()
        self.animatedScrollThumbY = thumbTargetY
        return
    end

    local shellAnimating = false
    local tabAnimating = false
    local alphaTarget = self.uiVisible and 1 or 0
    self.uiAlpha = LerpNumber(self.uiAlpha or alphaTarget, alphaTarget, SmoothFactor(16, dt))
    if math.abs(self.uiAlpha - alphaTarget) < 0.002 then
        self.uiAlpha = alphaTarget
    else
        shellAnimating = true
        tabAnimating = true
    end

    local offsetTarget = self.uiVisible and 0 or 10
    self.uiOffset = LerpNumber(self.uiOffset or offsetTarget, offsetTarget, SmoothFactor(16, dt))
    if math.abs(self.uiOffset - offsetTarget) >= 0.02 then
        shellAnimating = true
        tabAnimating = true
    else
        self.uiOffset = offsetTarget
    end

    self.renderX = self.window.x
    self.renderY = self.window.y

    local scaleTarget = self.uiVisible and 1 or 0.2
    self.renderScale = LerpNumber(self.renderScale or scaleTarget, scaleTarget, SmoothFactor(16, dt))
    if math.abs((self.renderScale or scaleTarget) - scaleTarget) >= 0.002 then
        shellAnimating = true
        tabAnimating = true
    else
        self.renderScale = scaleTarget
    end

    local contentTarget = self.uiVisible and 1 or 0
    self.contentAlpha = LerpNumber(self.contentAlpha or contentTarget, contentTarget, SmoothFactor(14, dt))
    self.contentOffset = LerpNumber(self.contentOffset or 0, self.uiVisible and 0 or 4, SmoothFactor(14, dt))
    if math.abs(self.contentAlpha - contentTarget) >= 0.002 or math.abs(self.contentOffset - (self.uiVisible and 0 or 4)) >= 0.02 then
        tabAnimating = true
    else
        self.contentAlpha = contentTarget
        self.contentOffset = self.uiVisible and 0 or 4
    end

    local activeTabTargetY = 110 + ((self.selectedTab - 1) * 32)
    if self.draggingWindow then
        self.activeTabHighlightY = activeTabTargetY
    else
        self.activeTabHighlightY = LerpNumber(self.activeTabHighlightY or activeTabTargetY, activeTabTargetY, SmoothFactor(18, dt))
    end
    if math.abs((self.activeTabHighlightY or activeTabTargetY) - activeTabTargetY) >= 0.02 then
        shellAnimating = true
    else
        self.activeTabHighlightY = activeTabTargetY
    end

    for tabIndex = 1, #self.tabs do
        local hoverTarget = self.hoveredId == ("tab_" .. tostring(tabIndex)) and 1 or 0
        local activeTarget = self.selectedTab == tabIndex and 1 or 0
        local _, hoverMoving = self:AnimateValue(self.animationState.tabHover, tabIndex, hoverTarget, 18, dt, 0)
        local _, activeMoving = self:AnimateValue(self.animationState.tabActive, tabIndex, activeTarget, 18, dt, activeTarget)
        shellAnimating = shellAnimating or hoverMoving or activeMoving
    end

    for _, tabControls in ipairs(self.controls or {}) do
        for _, control in ipairs(tabControls) do
            local hoverTarget = self.hoveredId == control.id and 1 or 0
            local _, hoverMoving = self:AnimateValue(self.animationState.controlHover, control.id, hoverTarget, 16, dt, 0)
            tabAnimating = tabAnimating or hoverMoving

            if control.type == "dropdown_single" or control.type == "dropdown_multi" then
                local _, moving = self:AnimateValue(self.animationState.dropdownOpen, control.id, self.expandedDropdown == control.id and 1 or 0, 18, dt, 0)
                tabAnimating = tabAnimating or moving
            elseif control.type == "slider" then
                local _, moving = self:AnimateValue(self.animationState.sliderFill, control.id, self:GetSliderPercent(control), 14, dt, self:GetSliderPercent(control))
                tabAnimating = tabAnimating or moving
            elseif control.type == "toggle" then
                local _, moving = self:AnimateValue(self.animationState.toggleFill, control.id, control.value and 1 or 0, 16, dt, control.value and 1 or 0)
                tabAnimating = tabAnimating or moving
            elseif control.type == "checkbox" then
                local _, moving = self:AnimateValue(self.animationState.checkboxFill, control.id, control.value and 1 or 0, 16, dt, control.value and 1 or 0)
                tabAnimating = tabAnimating or moving
            end
        end
    end

    local _, _, _, _, thumbTargetY = self:GetScrollbarMetrics()
    self.animatedScrollThumbY = LerpNumber(self.animatedScrollThumbY or thumbTargetY, thumbTargetY, SmoothFactor(16, dt))
    if math.abs((self.animatedScrollThumbY or thumbTargetY) - thumbTargetY) >= 0.02 then
        tabAnimating = true
    else
        self.animatedScrollThumbY = thumbTargetY
    end

    if shellAnimating then
        self:MarkShellDirty()
    end
    if tabAnimating then
        self:MarkCurrentTabDirty()
    end
end

function UI:DrawCardFrame(bucket, x, y, w, h, outlineColor)
    self:CreateSquare(bucket, x, y, w, h, Theme.card, 1, true)
    self:CreateSquare(bucket, x, y, w, h, outlineColor or Theme.border, 1, false, 1)
    self:CreateLine(bucket, x + 1, y + 1, x + w - 2, y + 1, Theme.topHighlight, 0.42, 1)
    self:CreateLine(bucket, x + 1, y + 2, x + w - 2, y + 2, Theme.cardInner, 0.2, 1)
end

function UI:RegisterHitbox(id, x, y, w, h, callback, priority)
    table.insert(self.hitboxes, {
        id = id,
        x = x,
        y = y,
        w = w,
        h = h,
        callback = callback,
        priority = priority or 0,
    })
end

function UI:IsPointInRect(point, x, y, w, h)
    return point.X >= x and point.X <= (x + w) and point.Y >= y and point.Y <= (y + h)
end

function UI:IsPointInMainWindow(point)
    local wx, wyBase = self:GetRenderWindowPosition()
    local wy = wyBase + self:GetAnimatedWindowYOffset()
    return self:IsPointInRect(point, wx, wy, self.window.width, self.window.height)
end

function UI:IsPointInLoadingWindow(point)
    local loadingScale = 0.4
    local loadW = math.floor(self.window.width * loadingScale)
    local loadH = math.floor(self.window.height * loadingScale)
    local viewport = self:GetViewportSize()
    local loadX = math.floor((viewport.X - loadW) * 0.5)
    local loadY = math.floor((viewport.Y - loadH) * 0.5)
    return self:IsPointInRect(point, loadX, loadY, loadW, loadH)
end

function UI:GetMouseLocation()
    local ok, pos = pcall(function()
        return UserInputService:GetMouseLocation()
    end)
    if ok and pos then
        return pos
    end

    if LocalMouse then
        return Vector2.new(LocalMouse.X or 0, LocalMouse.Y or 0)
    end

    return Vector2.new(0, 0)
end

function UI:IsMousePressed()
    local ok, pressed = pcall(function()
        return UserInputService:IsMouseButtonPressed(Enum.UserInputType.MouseButton1)
    end)
    if ok then
        return pressed == true
    end

    if type(ismouse1pressed) == "function" then
        local pressOk, result = pcall(ismouse1pressed)
        if pressOk then
            return result == true
        end
    end

    return false
end

function UI:GetActiveControls()
    return self.controls[self.selectedTab] or {}
end

function UI:GetControl(tabIndex, controlId)
    local tabControls = self.controls[tabIndex] or {}
    for _, control in ipairs(tabControls) do
        if control.id == controlId then
            return control
        end
    end
    return nil
end

function UI:SetStatValue(controlId, value)
    local control = self:GetControl(1, controlId)
    local nextValue = tostring(value or "")
    if control and control.value ~= nextValue then
        control.value = nextValue
        self:MarkTabDirty(1)
    end
end

function UI:GetLocalRootPart()
    local character = LocalPlayer and LocalPlayer.Character
    if not character then
        return nil
    end
    return character:FindFirstChild("HumanoidRootPart")
end

function UI:IsLocalPlayerOnCar()
    local playerFolder = LocalPlayer and workspace:FindFirstChild(LocalPlayer.Name)
    if not playerFolder then
        return false
    end

    for _, descendant in ipairs(playerFolder:GetDescendants()) do
        if type(descendant.Name) == "string" and string.find(descendant.Name, "Vehicle_", 1, true) then
            return true
        end
    end

    return false
end

function UI:GetLocalVehicleModel()
    local playerFolder = LocalPlayer and workspace:FindFirstChild(LocalPlayer.Name)
    if not playerFolder then
        return nil
    end

    for _, child in ipairs(playerFolder:GetChildren()) do
        if child:IsA("Model") and type(child.Name) == "string" and child.Name:sub(1, 8) == "Vehicle_" then
            return child
        end
    end

    for _, descendant in ipairs(playerFolder:GetDescendants()) do
        if descendant:IsA("Model") and type(descendant.Name) == "string" and descendant.Name:sub(1, 8) == "Vehicle_" then
            return descendant
        end
    end

    return nil
end

function UI:GetLocalVehicleDriverPart()
    local vehicleModel = self:GetLocalVehicleModel()
    if not vehicleModel then
        return nil
    end

    local driverPart = vehicleModel:FindFirstChild("Main")
        or vehicleModel:FindFirstChild("Chassi")
        or vehicleModel.PrimaryPart

    if driverPart and driverPart:IsA("BasePart") then
        return driverPart
    end

    for _, descendant in ipairs(vehicleModel:GetDescendants()) do
        if descendant:IsA("BasePart") then
            return descendant
        end
    end

    return nil
end

function UI:GetPlayerFolder()
    return LocalPlayer and workspace:FindFirstChild(LocalPlayer.Name) or nil
end

function UI:GetLocalPizzaBox()
    local playerFolder = self:GetPlayerFolder()
    return playerFolder and playerFolder:FindFirstChild("Pizza Box") or nil
end

function UI:GetLocalFoodCrate()
    local playerFolder = self:GetPlayerFolder()
    return playerFolder and playerFolder:FindFirstChild("Food Crate") or nil
end

function UI:GetRandomBffCrateSearchPosition()
    local rootPart = self:GetLocalRootPart()
    local baseY = rootPart and rootPart.Position.Y or BFF_JOB_POSITION.Y
    local minX = math.min(BFF_CRATE_AREA_MIN.X, BFF_CRATE_AREA_MAX.X)
    local maxX = math.max(BFF_CRATE_AREA_MIN.X, BFF_CRATE_AREA_MAX.X)
    local minZ = math.min(BFF_CRATE_AREA_MIN.Z, BFF_CRATE_AREA_MAX.Z)
    local maxZ = math.max(BFF_CRATE_AREA_MIN.Z, BFF_CRATE_AREA_MAX.Z)
    return Vector3.new(
        minX + (math.random() * (maxX - minX)),
        baseY,
        minZ + (math.random() * (maxZ - minZ))
    )
end

function UI:PressInteractKey()
    if type(setrobloxinput) == "function" then
        pcall(setrobloxinput, true)
    end

    if type(keypress) == "function" and type(keyrelease) == "function" then
        pcall(keypress, 0x45)
        task.wait(0.03)
        pcall(keyrelease, 0x45)
    end
end

function UI:ClickPrimary()
    if type(setrobloxinput) == "function" then
        pcall(setrobloxinput, true)
    end

    if type(mouse1click) == "function" then
        JobDebug("Goto Job: mouse1click start")
        task.spawn(function()
            pcall(mouse1click)
            JobDebug("Goto Job: mouse1click end")
        end)
        task.wait(0.08)
        return
    end

    if type(mouse1press) == "function" and type(mouse1release) == "function" then
        JobDebug("Goto Job: mouse1press start")
        task.spawn(function()
            pcall(mouse1press)
            JobDebug("Goto Job: mouse1press end")
        end)
        task.wait(0.05)
        JobDebug("Goto Job: mouse1release start")
        task.spawn(function()
            pcall(mouse1release)
            JobDebug("Goto Job: mouse1release end")
        end)
        task.wait(0.05)
    end
end

function UI:ClickGuiObject(guiObject, label)
    if not guiObject then
        JobDebug(string.format("Goto Job: missing gui object %s", tostring(label or "unknown")))
        return false
    end

    local absolutePosition = guiObject.AbsolutePosition
    local absoluteSize = guiObject.AbsoluteSize
    local centerX = math.floor(absolutePosition.X + (absoluteSize.X * 0.5))
    local centerY = math.floor(absolutePosition.Y + (absoluteSize.Y * 0.5))

    JobDebug(string.format(
        "Goto Job: clicking %s center=(%d, %d) size=(%d, %d)",
        tostring(label or guiObject.Name),
        centerX,
        centerY,
        math.floor(absoluteSize.X),
        math.floor(absoluteSize.Y)
    ))

    if type(setrobloxinput) == "function" then
        pcall(setrobloxinput, true)
    end

    if type(isrbxactive) == "function" then
        local activeOk, isActive = pcall(isrbxactive)
        JobDebug(string.format("Goto Job: roblox active=%s before %s", tostring(activeOk and isActive == true), tostring(label or guiObject.Name)))
    end

    task.spawn(function()
        if type(setrobloxinput) == "function" then
            pcall(setrobloxinput, true)
        end

        if type(mousemoveabs) == "function" then
            JobDebug(string.format("Goto Job: mousemoveabs to (%d, %d)", centerX, centerY))
            pcall(mousemoveabs, centerX, centerY)
            JobDebug("Goto Job: mousemoveabs end")
            task.wait(0.08)
        end

        if type(mousemoverel) == "function" then
            JobDebug("Goto Job: mousemoverel by (5, 0)")
            pcall(mousemoverel, 5, 0)
            JobDebug("Goto Job: mousemoverel end")
            task.wait(0.08)
        end

        JobDebug(string.format("Goto Job: primary click start for %s", tostring(label or guiObject.Name)))
        self:ClickPrimary()
        JobDebug(string.format("Goto Job: primary click end for %s", tostring(label or guiObject.Name)))
    end)

    task.wait(0.1)
    return true
end

function UI:IsPhoneOpen()
    local playerGui = LocalPlayer and LocalPlayer:FindFirstChild("PlayerGui")
    local hudGui = playerGui and playerGui:FindFirstChild("HUDGui")
    local phoneRoot = hudGui and hudGui:FindFirstChild("Phone")
    local phoneVertical = phoneRoot and phoneRoot:FindFirstChild("PhoneVertical")
    local canvasGroup = phoneVertical and phoneVertical:FindFirstChild("CanvasGroup")
    local appList = canvasGroup and canvasGroup:FindFirstChild("AppList")
    local appContainer = canvasGroup and canvasGroup:FindFirstChild("AppContainer")

    local phoneVisible = false
    local appListVisible = false
    local appContainerVisible = false

    if phoneRoot and phoneRoot:IsA("GuiObject") then
        phoneVisible = phoneRoot.Visible == true
    end
    if appList and appList:IsA("GuiObject") then
        appListVisible = appList.Visible == true
    end
    if appContainer and appContainer:IsA("GuiObject") then
        appContainerVisible = appContainer.Visible == true
    end

    return phoneVisible or appListVisible or appContainerVisible, phoneVisible, appListVisible, appContainerVisible
end

function UI:EnsurePhoneOpen()
    local playerGui = LocalPlayer and LocalPlayer:FindFirstChild("PlayerGui")
    local hudGui = playerGui and playerGui:FindFirstChild("HUDGui")
    local bottomRight = hudGui and hudGui:FindFirstChild("BottomRight")
    local phoneButton = bottomRight and bottomRight:FindFirstChild("Phone")

    for attempt = 1, 4 do
        local isOpen = self:IsPhoneOpen()
        if isOpen then
            JobDebug(string.format("Goto Job: phone already open on attempt %d", attempt))
            return true
        end

        if not self:ClickGuiObject(phoneButton, attempt == 1 and "Phone" or string.format("Phone Retry %d", attempt)) then
            return false
        end

        for waitAttempt = 1, 10 do
            local openNow, phoneVisible, appListVisible, appContainerVisible = self:IsPhoneOpen()
            JobDebug(string.format(
                "Goto Job: phone state attempt=%d wait=%d open=%s phoneVisible=%s appListVisible=%s appContainerVisible=%s",
                attempt,
                waitAttempt,
                tostring(openNow),
                tostring(phoneVisible),
                tostring(appListVisible),
                tostring(appContainerVisible)
            ))
            if openNow then
                return true
            end
            task.wait(0.15)
        end
    end

    JobDebug("Goto Job: phone failed to open")
    return false
end

function UI:GetGotoJobTeleportButton()
    local playerGui = LocalPlayer and LocalPlayer:FindFirstChild("PlayerGui")
    local hudGui = playerGui and playerGui:FindFirstChild("HUDGui")
    local phoneRoot = hudGui and hudGui:FindFirstChild("Phone")
    local phoneVertical = phoneRoot and phoneRoot:FindFirstChild("PhoneVertical")
    local canvasGroup = phoneVertical and phoneVertical:FindFirstChild("CanvasGroup")
    local appContainer = canvasGroup and canvasGroup:FindFirstChild("AppContainer")
    local jobListContent = appContainer and appContainer:FindFirstChild("JobListContent")
    local listContainer = jobListContent and jobListContent:FindFirstChild("ListContainer")
    local content = listContainer and listContainer:FindFirstChild("Content")
    local selectedJob = self:GetSelectedJob()

    if selectedJob == "Pizza Delivery Job" then
        local pizzaJob = content and content:FindFirstChild("PizzaPlanetDelivery")
        return pizzaJob and pizzaJob:FindFirstChild("TeleportButton") or nil
    end

    if selectedJob == "Bloxburg Taxi" then
        local taxiJob = content and content:FindFirstChild("TaxiJob")
        return taxiJob and taxiJob:FindFirstChild("TeleportButton") or nil
    end

    return nil
end

function UI:GetGotoJobJobsButton()
    local playerGui = LocalPlayer and LocalPlayer:FindFirstChild("PlayerGui")
    local hudGui = playerGui and playerGui:FindFirstChild("HUDGui")
    local phoneRoot = hudGui and hudGui:FindFirstChild("Phone")
    local phoneVertical = phoneRoot and phoneRoot:FindFirstChild("PhoneVertical")
    local canvasGroup = phoneVertical and phoneVertical:FindFirstChild("CanvasGroup")
    local appList = canvasGroup and canvasGroup:FindFirstChild("AppList")
    local pages = appList and appList:FindFirstChild("Pages")
    local page1 = pages and pages:FindFirstChild("Page_1")
    local pageCanvasGroup = page1 and page1:FindFirstChild("CanvasGroup")
    local jobList = pageCanvasGroup and pageCanvasGroup:FindFirstChild("JobList")
    return jobList and jobList:FindFirstChild("Button") or nil
end

function UI:WaitForGuiObject(getter, label, attempts, delaySeconds)
    local tries = attempts or 10
    local delayTime = delaySeconds or 0.15

    for index = 1, tries do
        local guiObject = getter()
        if guiObject then
            JobDebug(string.format("Goto Job: found %s on attempt %d", tostring(label), index))
            return guiObject
        end
        task.wait(delayTime)
    end

    JobDebug(string.format("Goto Job: failed to find %s after %d attempts", tostring(label), tries))
    return nil
end

function UI:GotoSelectedJob()
    JobDebug(string.format("Goto Job: selected job=%s", tostring(self:GetSelectedJob())))
    if not self:EnsurePhoneOpen() then
        return
    end

    local jobsButton = self:WaitForGuiObject(function()
        return self:GetGotoJobJobsButton()
    end, "Jobs", 12, 0.15)
    if not self:ClickGuiObject(jobsButton, "Jobs") then
        return
    end

    local teleportButton = self:WaitForGuiObject(function()
        return self:GetGotoJobTeleportButton()
    end, "Teleport", 12, 0.15)
    self:ClickGuiObject(teleportButton, "Teleport")
end

function UI:GetCustomerTargetPosition()
    local mouseIgnore = workspace:FindFirstChild("MouseIgnore")
    local guidingArrow = mouseIgnore and mouseIgnore:FindFirstChild("GuidingArrow_PizzaDelivery_Customer")
    local gameFolder = workspace:FindFirstChild("_game")
    local spawnedCharacters = gameFolder and gameFolder:FindFirstChild("SpawnedCharacters")
    if not guidingArrow or not guidingArrow:IsA("BasePart") then
        FarmDebug("Customer lookup: guiding arrow missing")
        return nil
    end

    if not spawnedCharacters then
        FarmDebug("Customer lookup: _game/SpawnedCharacters missing")
        return nil
    end

    local arrowPosition = guidingArrow.Position
    local arrowLook = guidingArrow.CFrame.LookVector
    local bestModel = nil
    local bestPosition = nil
    local bestDot = -1
    local bestDistance = math.huge

    FarmDebug(string.format(
        "Customer lookup arrow: pos=(%.2f, %.2f, %.2f) look=(%.3f, %.3f, %.3f)",
        arrowPosition.X,
        arrowPosition.Y,
        arrowPosition.Z,
        arrowLook.X,
        arrowLook.Y,
        arrowLook.Z
    ))

    for _, customer in ipairs(spawnedCharacters:GetChildren()) do
        local customerName = tostring(customer and customer.Name or "")
        local loweredName = string.lower(customerName)
        if customer:IsA("Model")
            and string.find(loweredName, "pizza", 1, true)
            and string.find(loweredName, "customer", 1, true)
        then
            local candidatePart = customer:FindFirstChild("HumanoidRootPart")
            if not (candidatePart and candidatePart:IsA("BasePart")) then
                candidatePart = customer:FindFirstChild("Head")
            end
            if not (candidatePart and candidatePart:IsA("BasePart")) then
                candidatePart = customer.PrimaryPart
            end
            if not (candidatePart and candidatePart:IsA("BasePart")) then
                for _, descendant in ipairs(customer:GetDescendants()) do
                    if descendant:IsA("BasePart") then
                        candidatePart = descendant
                        break
                    end
                end
            end

            if candidatePart and candidatePart:IsA("BasePart") then
                local candidatePosition = candidatePart.Position
                local offset = candidatePosition - arrowPosition
                local distance = offset.Magnitude

                if distance > 2 then
                    local direction = offset.Unit
                    local dot = (arrowLook.X * direction.X) + (arrowLook.Y * direction.Y) + (arrowLook.Z * direction.Z)

                    FarmDebug(string.format(
                        "Customer lookup candidate: name=%s dot=%.4f distance=%.2f pos=(%.2f, %.2f, %.2f)",
                        customerName,
                        dot,
                        distance,
                        candidatePosition.X,
                        candidatePosition.Y,
                        candidatePosition.Z
                    ))

                    if dot > 0 and (dot > bestDot or (math.abs(dot - bestDot) <= 0.0001 and distance < bestDistance)) then
                        bestModel = customer
                        bestPosition = candidatePosition
                        bestDot = dot
                        bestDistance = distance
                    end
                end
            else
                FarmDebug(string.format("Customer lookup candidate skipped: name=%s invalid_part", customerName))
            end
        end
    end

    if bestModel and bestPosition and bestDot > 0.85 then
        FarmDebug(string.format(
            "Customer lookup result: name=%s dot=%.4f distance=%.2f target=(%.2f, %.2f, %.2f)",
            tostring(bestModel.Name),
            bestDot,
            bestDistance,
            bestPosition.X,
            bestPosition.Y,
            bestPosition.Z
        ))
        return bestPosition
    end

    FarmDebug(string.format(
        "Customer lookup result: no valid customer bestDot=%s",
        bestDot >= 0 and string.format("%.4f", bestDot) or "nil"
    ))
    return nil
end

function UI:GetCharacterTeleportPart(character)
    if not character then
        return nil
    end

    local rootPart = character:FindFirstChild("HumanoidRootPart")
        or character:FindFirstChild("UpperTorso")
        or character:FindFirstChild("Torso")
        or character:FindFirstChild("Head")

    if rootPart and rootPart:IsA("BasePart") then
        return rootPart
    end

    local primaryPart = character.PrimaryPart
    if primaryPart and primaryPart:IsA("BasePart") then
        return primaryPart
    end

    for _, child in ipairs(character:GetChildren()) do
        if child:IsA("BasePart") then
            return child
        end
    end

    return nil
end

function UI:GetTweenSpeed()
    local control = self:GetControl(self.settingsTabIndex or 4, "tween_speed")
    local value = control and tonumber(control.value) or 20
    return math.clamp(value or 20, 5, 40)
end

function UI:IsCarFlyEnabled()
    local control = self:GetControl(self.premiumTabIndex or 6, "car_fly_toggle")
    if not self:CanUsePremiumFeature() then
        return false
    end
    return control and control.value == true or false
end

function UI:GetCarFlySpeed()
    local control = self:GetControl(self.premiumTabIndex or 6, "car_fly_speed")
    local value = control and tonumber(control.value) or self.carFlySpeed or 80
    return math.clamp(value or 80, 20, 140)
end

function UI:IsCarSpeedEnabled()
    local control = self:GetControl(self.premiumTabIndex or 6, "car_speed_toggle")
    if not self:CanUsePremiumFeature() then
        return false
    end
    return control and control.value == true or false
end

function UI:GetCarSpeedMultiplier()
    local control = self:GetControl(self.premiumTabIndex or 6, "car_speed_multiplier")
    local value = control and tonumber(control.value) or 1.6
    return math.clamp(value or 1.6, 0.1, 3)
end

function UI:GetLocalVehicleSpeedModifier()
    local vehicleModel = self:GetLocalVehicleModel()
    if not vehicleModel then
        return nil
    end

    local modifier = vehicleModel:FindFirstChild("VehicleSpeedModifier")
    if modifier and tonumber(modifier.Value) ~= nil then
        return modifier
    end

    for _, descendant in ipairs(vehicleModel:GetDescendants()) do
        if descendant.Name == "VehicleSpeedModifier" and tonumber(descendant.Value) ~= nil then
            return descendant
        end
    end

    return nil
end

function UI:ReadMemoryFloat(address)
    local ok, value = pcall(function()
        return memory_read("float", address)
    end)
    if ok and value then
        return value
    end
    return nil
end

function UI:GetCarFlyCameraDirections()
    local camera = workspace and workspace.CurrentCamera
    if not camera or not camera.Address then
        return nil, nil
    end

    local base = camera.Address + 0xF8
    local r00 = self:ReadMemoryFloat(base + 0x00)
    local r02 = self:ReadMemoryFloat(base + 0x08)
    local r10 = self:ReadMemoryFloat(base + 0x0C)
    local r12 = self:ReadMemoryFloat(base + 0x14)
    local r20 = self:ReadMemoryFloat(base + 0x18)
    local r22 = self:ReadMemoryFloat(base + 0x20)

    if not r00 or not r02 or not r10 or not r12 or not r20 or not r22 then
        return nil, nil
    end

    local right = Vector3.new(r00, r10, r20)
    local look = Vector3.new(-r02, -r12, -r22)
    if right.Magnitude <= 0 or look.Magnitude <= 0 then
        return nil, nil
    end

    return look.Unit, right.Unit
end

function UI:SetPartPositionKeepRotation(part, position)
    if not part or typeof(position) ~= "Vector3" then
        return
    end

    local current = part.CFrame
    part.CFrame = CFrame.new(position.X, position.Y, position.Z) * (current - current.Position)
end

function UI:HoldInteractionTarget(targetPosition)
    if typeof(targetPosition) ~= "Vector3" then
        return false
    end

    local rootPart = self:GetLocalRootPart()
    if not rootPart then
        return false
    end

    local currentRootPosition = rootPart.Position
    local delta = targetPosition - currentRootPosition
    if delta.Magnitude <= 0.25 then
        return true
    end

    local driverPart = self:GetLocalVehicleDriverPart()
    self:ZeroPartPhysics(rootPart)
    rootPart.Position = targetPosition

    if driverPart then
        self:ZeroPartPhysics(driverPart)
        driverPart.Position = driverPart.Position + delta
    end

    return true
end

function UI:UpdateCarFly(dt)
    local enabled = self:IsCarFlyEnabled()
    if not enabled then
        self.carFlyEnabled = false
        self.carFlyActive = false
        self.carFlyPosition = nil
        self.carFlyTargetPosition = nil
        return
    end

    self.carFlyEnabled = true

    local now = os.clock()
    if type(iskeypressed) == "function" and iskeypressed(0x46) and (now - (self.lastCarFlyToggleTick or 0)) >= 0.3 then
        self.lastCarFlyToggleTick = now
        self.carFlyActive = not self.carFlyActive
        local driverPart = self:GetLocalVehicleDriverPart()
        self.carFlyPosition = driverPart and driverPart.Position or nil
        self.carFlyTargetPosition = driverPart and driverPart.Position or nil
        self.lastCarFlyTick = now
    end

    if not self.carFlyActive then
        return
    end

    local driverPart = self:GetLocalVehicleDriverPart()
    if not driverPart then
        self.carFlyActive = false
        self.carFlyPosition = nil
        self.carFlyTargetPosition = nil
        return
    end

    local rootPart = self:GetLocalRootPart()
    local look, right = self:GetCarFlyCameraDirections()
    if not look or not right then
        return
    end

    local frameDt = tonumber(dt) or 0.016
    if frameDt <= 0 or frameDt > 0.2 then
        frameDt = 0.016
    end

    if typeof(self.carFlyPosition) ~= "Vector3" then
        self.carFlyPosition = driverPart.Position
    end
    if typeof(self.carFlyTargetPosition) ~= "Vector3" then
        self.carFlyTargetPosition = self.carFlyPosition
    end

    local moveDirection = Vector3.new(0, 0, 0)
    if iskeypressed(0x57) then moveDirection = moveDirection + look end
    if iskeypressed(0x53) then moveDirection = moveDirection - look end
    if iskeypressed(0x41) then moveDirection = moveDirection - right end
    if iskeypressed(0x44) then moveDirection = moveDirection + right end
    if iskeypressed(0x20) then moveDirection = moveDirection + Vector3.new(0, 1, 0) end
    if iskeypressed(0x51) then moveDirection = moveDirection - Vector3.new(0, 1, 0) end

    if moveDirection.Magnitude > 0 then
        moveDirection = moveDirection.Unit
        self.carFlyTargetPosition = self.carFlyTargetPosition + (moveDirection * self:GetCarFlySpeed() * frameDt)
    end

    local smoothAlpha = math.clamp(frameDt * 28, 0, 1)
    self.carFlyPosition = self.carFlyPosition:Lerp(self.carFlyTargetPosition, smoothAlpha)

    self:ZeroPartPhysics(driverPart)
    self:SetPartPositionKeepRotation(driverPart, self.carFlyPosition)

    if rootPart then
        self:ZeroPartPhysics(rootPart)
    end
end

function UI:UpdateCarSpeedModifier()
    if not self:IsCarSpeedEnabled() then
        return
    end

    local modifier = self:GetLocalVehicleSpeedModifier()
    if not modifier then
        return
    end

    local targetValue = self:GetCarSpeedMultiplier()
    if math.abs((tonumber(modifier.Value) or 0) - targetValue) > 0.001 then
        modifier.Value = targetValue
    end
end

function UI:GetPizzaBikeEspPart()
    local environment = workspace and workspace:FindFirstChild("Environment")
    local locations = environment and environment:FindFirstChild("Locations")
    local city = locations and locations:FindFirstChild("City")
    local pizzaPlanet = city and city:FindFirstChild("PizzaPlanet")
    local geometry = pizzaPlanet and pizzaPlanet:FindFirstChild("Geometry")
    local deliveryMoped = geometry and geometry:FindFirstChild("DeliveryMoped")
    local vehicle = deliveryMoped and deliveryMoped:FindFirstChild("Vehicle")
    local body = vehicle and vehicle:FindFirstChild("Body")

    if body and body:IsA("BasePart") then
        return body
    end

    if body then
        for _, descendant in ipairs(body:GetDescendants()) do
            if descendant:IsA("BasePart") then
                return descendant
            end
        end
    end

    if vehicle then
        for _, descendant in ipairs(vehicle:GetDescendants()) do
            if descendant:IsA("BasePart") then
                return descendant
            end
        end
    end

    return nil
end

function UI:IsOnPizzaDeliveryMoped()
    local vehicleModel = self:GetLocalVehicleModel()
    return vehicleModel and vehicleModel.Name == "Vehicle_Delivery Moped" or false
end

function UI:IsPizzaBikeEspEnabled()
    local control = self:GetControl(self.debugTabIndex or 5, "pizza_bike_esp")
    return control and control.value == true or false
end

function UI:UpdatePizzaBikeEsp()
    local enabled = self:IsPizzaBikeEspEnabled()
    local key = "debug_pizza_bike"
    local seen = {}
    self.lastPizzaBikeEspDebugAt = self.lastPizzaBikeEspDebugAt or 0
    self.lastPizzaBikeEspDebugMessage = self.lastPizzaBikeEspDebugMessage or ""
    local function logState(message)
        local now = tick()
        if message ~= self.lastPizzaBikeEspDebugMessage or (now - self.lastPizzaBikeEspDebugAt) >= 1 then
            self.lastPizzaBikeEspDebugMessage = message
            self.lastPizzaBikeEspDebugAt = now
            EspDebug(message)
        end
    end

    if not enabled then
        for trackedKey, _ in pairs(self.activeEspKeys.debug or {}) do
            local trackedEntry = self.espObjects[trackedKey]
            if trackedEntry then
                self:HideEspEntry(trackedEntry)
            end
        end
        self.activeEspKeys.debug = {}
        return
    end

    local part = self:GetPizzaBikeEspPart()
    if not part then
        logState("Pizza Bike Esp: target part missing")
        for trackedKey, _ in pairs(self.activeEspKeys.debug or {}) do
            local trackedEntry = self.espObjects[trackedKey]
            if trackedEntry then
                self:HideEspEntry(trackedEntry)
            end
        end
        self.activeEspKeys.debug = {}
        return
    end

    local entry = self:GetEspEntry(key)
    local topOffset = math.max(1.5, (part.Size.Y * 0.5) + 0.6)
    local bottomOffset = math.max(1.5, (part.Size.Y * 0.5) + 0.6)
    local headPoint, headVisible = self:GetWorldToScreen(part.Position + Vector3.new(0, topOffset, 0))
    local footPoint, footVisible = self:GetWorldToScreen(part.Position - Vector3.new(0, bottomOffset, 0))

    if not headPoint or not footPoint or not headVisible or not footVisible then
        logState(string.format(
            "Pizza Bike Esp: offscreen part=%s pos=(%.2f, %.2f, %.2f) onScreen=%s",
            tostring(part:GetFullName()),
            part.Position.X,
            part.Position.Y,
            part.Position.Z,
            tostring(headVisible == true and footVisible == true)
        ))
        self:HideEspEntry(entry)
        self:CleanupTrackedEspKeys("debug", seen)
        return
    end

    local camera = workspace and workspace.CurrentCamera or nil
    if not camera then
        logState("Pizza Bike Esp: camera missing")
        self:HideEspEntry(entry)
        self:CleanupTrackedEspKeys("debug", seen)
        return
    end

    local boxHeight = math.max(18, math.abs(footPoint.Y - headPoint.Y))
    local boxWidth = math.max(12, math.floor(boxHeight * 0.55))
    local boxX = math.floor(headPoint.X - (boxWidth / 2))
    local boxY = math.floor(headPoint.Y)

    local distance = (camera.CFrame.Position - part.Position).Magnitude
    entry.box.Position = Vector2.new(boxX, boxY)
    entry.box.Size = Vector2.new(boxWidth, boxHeight)
    entry.box.Color = Theme.accent
    entry.box.Transparency = 1
    entry.box.Visible = true

    entry.label.Text = "Pizza Bike"
    entry.label.Position = Vector2.new(math.floor(headPoint.X), math.floor(boxY - 16))
    entry.label.Color = Theme.text
    entry.label.Transparency = 1
    entry.label.Visible = true
    seen[key] = true
    self.activeEspKeys.debug[key] = true
    self:CleanupTrackedEspKeys("debug", seen)

    logState(string.format(
        "Pizza Bike Esp: drawing part=%s screen=(%.1f, %.1f) distance=%.2f",
        tostring(part:GetFullName()),
        headPoint.X,
        headPoint.Y,
        distance
    ))
end

function UI:IsPathEspEnabled()
    local control = self:GetControl(self.debugTabIndex or 5, "path_esp")
    return control and control.value == true or false
end

function UI:IsNodeEspEnabled()
    local control = self:GetControl(self.debugTabIndex or 5, "node_esp")
    return control and control.value == true or false
end

function UI:EnsurePathDebugDrawings()
    if self.pathDebugDrawings then
        return self.pathDebugDrawings
    end

    local drawings = {
        currentLine = Drawing.new("Line"),
        currentCircle = Drawing.new("Circle"),
        currentLabel = Drawing.new("Text"),
        nodeLines = {},
        nodeCircles = {},
        nodeLabels = {},
    }

    drawings.currentLine.Thickness = 2
    drawings.currentLine.Transparency = 1
    drawings.currentLine.Color = Theme.accent
    drawings.currentLine.Visible = false

    drawings.currentCircle.Radius = 5
    drawings.currentCircle.Filled = true
    drawings.currentCircle.Transparency = 1
    drawings.currentCircle.Color = Theme.accent
    drawings.currentCircle.Visible = false

    drawings.currentLabel.Center = true
    drawings.currentLabel.Outline = true
    drawings.currentLabel.Font = 2
    drawings.currentLabel.Size = 13
    drawings.currentLabel.Transparency = 1
    drawings.currentLabel.Color = Theme.text
    drawings.currentLabel.Visible = false

    for index = 1, 24 do
        local line = Drawing.new("Line")
        line.Thickness = 2
        line.Transparency = 1
        line.Color = Theme.accentSoft
        line.Visible = false
        drawings.nodeLines[index] = line

        local circle = Drawing.new("Circle")
        circle.Radius = 4
        circle.Filled = true
        circle.Transparency = 1
        circle.Color = Theme.accentSoft
        circle.Visible = false
        drawings.nodeCircles[index] = circle

        local label = Drawing.new("Text")
        label.Center = true
        label.Outline = true
        label.Font = 2
        label.Size = 12
        label.Transparency = 1
        label.Color = Theme.text
        label.Visible = false
        drawings.nodeLabels[index] = label
    end

    self.pathDebugDrawings = drawings
    return drawings
end

function UI:HidePathDebugDrawings()
    if not self.pathDebugDrawings then
        return
    end

    local drawings = self.pathDebugDrawings
    drawings.currentLine.Visible = false
    drawings.currentCircle.Visible = false
    drawings.currentLabel.Visible = false

    for _, line in ipairs(drawings.nodeLines or {}) do
        line.Visible = false
    end
    for _, circle in ipairs(drawings.nodeCircles or {}) do
        circle.Visible = false
    end
    for _, label in ipairs(drawings.nodeLabels or {}) do
        label.Visible = false
    end
end

function UI:UpdatePathDebugDrawings()
    local pathEspEnabled = self:IsPathEspEnabled()
    local nodeEspEnabled = self:IsNodeEspEnabled()
    if not self:IsFarmEnabled() or (not pathEspEnabled and not nodeEspEnabled) then
        self:HidePathDebugDrawings()
        return
    end

    local rootPart = self:GetLocalRootPart()
    if not rootPart then
        self:HidePathDebugDrawings()
        return
    end

    local drawings = self:EnsurePathDebugDrawings()
    self:HidePathDebugDrawings()

    local currentTarget = WalkToService.currentTarget
    local nodes = self.pathNodes or {}
    local routeTargets = {}

    if typeof(currentTarget) == "Vector3" then
        routeTargets[#routeTargets + 1] = currentTarget
    else
        for _, node in ipairs(nodes) do
            if type(node) == "table" and typeof(node.position) == "Vector3" then
                routeTargets[#routeTargets + 1] = node.position
            end
        end
    end

    if pathEspEnabled and typeof(currentTarget) == "Vector3" then
        local startPoint, startVisible = self:GetWorldToScreen(rootPart.Position)
        local targetPoint, targetVisible = self:GetWorldToScreen(currentTarget)
        if startPoint and targetPoint and startVisible and targetVisible then
            drawings.currentLine.From = startPoint
            drawings.currentLine.To = targetPoint
            drawings.currentLine.Color = Theme.accent
            drawings.currentLine.Visible = true

            drawings.currentCircle.Position = targetPoint
            drawings.currentCircle.Color = Theme.accent
            drawings.currentCircle.Visible = true

            drawings.currentLabel.Text = "Walk Target"
            drawings.currentLabel.Position = Vector2.new(targetPoint.X, targetPoint.Y - 18)
            drawings.currentLabel.Visible = true
        end
    elseif pathEspEnabled and #routeTargets > 0 then
        local startPoint, startVisible = self:GetWorldToScreen(rootPart.Position)
        local firstPoint, firstVisible = self:GetWorldToScreen(routeTargets[1])
        if startPoint and firstPoint and startVisible and firstVisible then
            drawings.currentLine.From = startPoint
            drawings.currentLine.To = firstPoint
            drawings.currentLine.Color = Theme.accent
            drawings.currentLine.Visible = true

            drawings.currentCircle.Position = firstPoint
            drawings.currentCircle.Color = Theme.accent
            drawings.currentCircle.Visible = true

            drawings.currentLabel.Text = "Planned Path"
            drawings.currentLabel.Position = Vector2.new(firstPoint.X, firstPoint.Y - 18)
            drawings.currentLabel.Visible = true
        end
    end

    local previousScreenPoint = nil
    local previousVisible = false
    local rootScreenPoint = nil
    local rootVisible = false

    if pathEspEnabled then
        rootScreenPoint, rootVisible = self:GetWorldToScreen(rootPart.Position)
    end

    for index = 1, math.min(#nodes, #(drawings.nodeCircles or {})) do
        local nodeEntry = nodes[index]
        local nodePosition = type(nodeEntry) == "table" and nodeEntry.position or nil
        local nodeName = type(nodeEntry) == "table" and tostring(nodeEntry.name or string.format("Node %d", index)) or string.format("Node %d", index)
        local screenPoint, visible = self:GetWorldToScreen(nodePosition)
        if screenPoint and visible then
            if nodeEspEnabled then
                drawings.nodeCircles[index].Position = screenPoint
                drawings.nodeCircles[index].Visible = true

                drawings.nodeLabels[index].Text = nodeName
                drawings.nodeLabels[index].Position = Vector2.new(screenPoint.X, screenPoint.Y - 14)
                drawings.nodeLabels[index].Visible = true
            end

            if pathEspEnabled and index == 1 and rootScreenPoint and rootVisible then
                drawings.nodeLines[index].From = rootScreenPoint
                drawings.nodeLines[index].To = screenPoint
                drawings.nodeLines[index].Visible = true
            elseif pathEspEnabled and previousScreenPoint and previousVisible then
                drawings.nodeLines[index].From = previousScreenPoint
                drawings.nodeLines[index].To = screenPoint
                drawings.nodeLines[index].Visible = true
            end

            previousScreenPoint = screenPoint
            previousVisible = true
        else
            previousScreenPoint = nil
            previousVisible = false
        end
    end
end

function UI:ApplyVelocityHold(part)
    if not part then
        return
    end

    pcall(function()
        part.AssemblyLinearVelocity = Vector3.new(0, 0, 0)
    end)
    pcall(function()
        part.Velocity = Vector3.new(0, 0, 0)
    end)
    pcall(function()
        part.RotVelocity = Vector3.new(0, 0, 0)
    end)
end

function UI:ZeroPartPhysics(part)
    if not part then
        return
    end

    pcall(function()
        part.Velocity = Vector3.new(0, 0, 0)
    end)
    pcall(function()
        part.AssemblyLinearVelocity = Vector3.new(0, 0, 0)
    end)
    pcall(function()
        part.RotVelocity = Vector3.new(0, 0, 0)
    end)
    pcall(function()
        part.AssemblyAngularVelocity = Vector3.new(0, 0, 0)
    end)
end

function UI:SetTweenNoclipEnabled(enabled)
    local character = LocalPlayer and LocalPlayer.Character
    if not character then
        return
    end

    if enabled then
        if self.tweenNoclipActive then
            return
        end

        self.tweenNoclipStates = {}
        for _, child in ipairs(character:GetChildren()) do
            if child:IsA("BasePart") then
                self.tweenNoclipStates[child] = child.CanCollide
                child.CanCollide = false
            end
        end
        self.tweenNoclipActive = true
        return
    end

    if not self.tweenNoclipActive then
        return
    end

    for part, oldCanCollide in pairs(self.tweenNoclipStates or {}) do
        if part and part.Parent then
            part.CanCollide = oldCanCollide == true
        end
    end
    self.tweenNoclipStates = {}
    self.tweenNoclipActive = false
end

function UI:SetWorldGravity(gravity)
    if type(memory_read) ~= "function" or type(memory_write) ~= "function" or not workspace or not workspace.Address then
        return false
    end

    local worldPtr = memory_read("uintptr_t", workspace.Address + 0x408)
    if not worldPtr or worldPtr == 0 then
        return false
    end

    pcall(function()
        memory_write("float", worldPtr + 0x210, gravity)
    end)
    return true
end

function UI:SyncTweenGravity(rootPart)
    local shouldForceZero = self.carFlyActive == true
        or (self:IsFarmEnabled() and self:GetSelectedJob() ~= "BFF Supermarket")

    if shouldForceZero and not self.tweenGravityActive then
        self:SetWorldGravity(0)
        self.tweenGravityActive = true
        return
    end

    if (not shouldForceZero) and self.tweenGravityActive then
        self:SetWorldGravity(196.2)
        self.tweenGravityActive = false
    end
end

function UI:CancelTweenToPositions(reason)
    if self.tweenState and self.tweenState.active then
        TweenDebug(string.format(
            "Tween worker cancelled id=%d reason=%s",
            tonumber(self.tweenState.tweenId) or 0,
            tostring(reason or "unknown")
        ))
    end

    self.activeTweenId = (self.activeTweenId or 0) + 1
    self:SetTweenNoclipEnabled(false)
    self.tweenState.active = false
    self:SyncTweenGravity(nil)
    self.tweenState.stages = nil
    self.tweenState.stageIndex = 0
    self.tweenState.stageStart = nil
    self.tweenState.stageTarget = nil
    self.tweenState.stageDuration = 0
    self.tweenState.stageStartedAt = 0
    self.tweenState.driverPart = nil
    self.tweenState.driverStart = nil
    self.tweenState.driverTarget = nil
    self.tweenState.rootStart = nil
    self.tweenState.stageProvider = nil
    self.tweenState.totalStages = 0
    self.tweenState.requiresCar = false
end

function UI:BeginTweenStage(stageTarget)
    local state = self.tweenState
    local rootPart = self:GetLocalRootPart()
    if not state.active or not rootPart or typeof(stageTarget) ~= "Vector3" then
        return false
    end

    local rootStart = rootPart.Position
    local driverPart = self:GetLocalVehicleDriverPart()
    local driverTarget = nil
    local driverStart = driverPart and driverPart.Position or nil
    if driverPart then
        local delta = stageTarget - rootPart.Position
        driverTarget = driverPart.Position + delta
    end

    local distance = (stageTarget - rootStart).Magnitude
    if distance < 1 then
        state.rootStart = rootStart
        state.stageStart = rootStart
        state.stageTarget = stageTarget
        if driverPart and typeof(driverTarget) == "Vector3" then
            driverPart.Position = driverTarget
        end
        rootPart.Position = stageTarget
        self:ApplyVelocityHold(rootPart)
        self:ApplyVelocityHold(driverPart)
        return self:AdvanceTweenStage()
    end

    state.rootStart = rootStart
    state.stageStart = rootStart
    state.stageTarget = stageTarget
    state.driverPart = driverPart
    state.driverStart = driverStart
    state.driverTarget = driverTarget
    state.stageDuration = math.max(distance / math.max(state.speed or 1, 0.01), TWEEN_MIN_STAGE_DURATION)
    state.stageStartedAt = os.clock()
    state.requiresCar = driverPart ~= nil and typeof(driverTarget) == "Vector3"
    return true
end

function UI:AdvanceTweenStage()
    local state = self.tweenState
    if not state.active then
        return false
    end

    local nextIndex = (state.stageIndex or 0) + 1
    if nextIndex > (state.totalStages or 0) then
        self:CancelTweenToPositions("complete")
        return false
    end

    local rootPart = self:GetLocalRootPart()
    if not rootPart then
        self:CancelTweenToPositions("missing_root")
        return false
    end

    local stageTarget = state.stageProvider and state.stageProvider(nextIndex, rootPart) or nil
    if typeof(stageTarget) ~= "Vector3" then
        self:CancelTweenToPositions("missing_target")
        return false
    end

    state.stageIndex = nextIndex
    return self:BeginTweenStage(stageTarget)
end

function UI:UpdateActiveTween()
    local state = self.tweenState
    if not state or not state.active then
        return
    end

    local rootPart = self:GetLocalRootPart()
    if not rootPart then
        self:CancelTweenToPositions("missing_root")
        return
    end

    if state.requiresCar and not self:IsLocalPlayerOnCar() then
        self:CancelTweenToPositions("car_lost")
        return
    end

    local elapsed = os.clock() - (state.stageStartedAt or os.clock())
    local alpha = math.clamp(elapsed / math.max(state.stageDuration or TWEEN_MIN_STAGE_DURATION, TWEEN_MIN_STAGE_DURATION), 0, 1)

    self:SyncTweenGravity(rootPart)

    self:ApplyVelocityHold(rootPart)
    if state.rootStart and typeof(state.stageTarget) == "Vector3" then
        rootPart.Position = state.rootStart:Lerp(state.stageTarget, alpha)
    end

    local driverPart = state.driverPart
    if driverPart and state.driverStart and typeof(state.driverTarget) == "Vector3" then
        self:ApplyVelocityHold(driverPart)
        driverPart.Position = state.driverStart:Lerp(state.driverTarget, alpha)
    end

    if alpha >= 1 then
        if typeof(state.stageTarget) == "Vector3" then
            rootPart.Position = state.stageTarget
        end
        if driverPart and typeof(state.driverTarget) == "Vector3" then
            driverPart.Position = state.driverTarget
        end
        self:ApplyVelocityHold(rootPart)
        self:ApplyVelocityHold(driverPart)
        self:AdvanceTweenStage()
    end
end

function UI:TweenToPositions(targetPositions)
    local firstTarget = nil
    for _, targetPosition in ipairs(targetPositions or {}) do
        if typeof(targetPosition) == "Vector3" then
            firstTarget = targetPosition
            break
        end
    end

    if firstTarget then
        local canTween = self:CanTweenToTarget(firstTarget, true)
        if not canTween then
            return
        end
    end

    self:CancelTweenToPositions("restart")
    local tweenId = (self.activeTweenId or 0) + 1
    self.activeTweenId = tweenId
    local stages = {}

    for _, targetPosition in ipairs(targetPositions or {}) do
        if typeof(targetPosition) == "Vector3" then
            stages[#stages + 1] = targetPosition
        end
    end

    TweenDebug(string.format("Queue tween id=%d stages=%d", tweenId, #stages))
    if #stages <= 0 then
        return
    end

    self.tweenState.active = true
    self.tweenState.tweenId = tweenId
    self.tweenState.stages = stages
    self.tweenState.stageProvider = function(index)
        return stages[index]
    end
    self.tweenState.totalStages = #stages
    self.tweenState.stageIndex = 0
    self.tweenState.speed = self:GetTweenSpeed() * TWEEN_SPEED_SCALE
    self:SetTweenNoclipEnabled(true)
    self:AdvanceTweenStage()
end

function UI:TweenWithVehicleTo(targetPosition)
    local rootPart = self:GetLocalRootPart()
    if not rootPart or typeof(targetPosition) ~= "Vector3" then
        return false
    end

    local canTween, distanceToTarget = self:CanTweenToTarget(targetPosition, true)
    if not canTween then
        return false
    end

    self:CancelTweenToPositions("restart")
    local tweenId = (self.activeTweenId or 0) + 1
    self.activeTweenId = tweenId
    self.tweenState.active = true
    self.tweenState.tweenId = tweenId
    self.tweenState.totalStages = distanceToTarget < 50 and 1 or 3
    self.tweenState.stageIndex = 0
    self.tweenState.speed = self:GetTweenSpeed() * TWEEN_SPEED_SCALE
    self.tweenState.stageProvider = function(index, liveRootPart)
        if distanceToTarget < 50 then
            if index == 1 then
                return targetPosition
            end
            return nil
        end
        if index == 1 then
            return Vector3.new(liveRootPart.Position.X, -40, liveRootPart.Position.Z)
        end
        if index == 2 then
            return Vector3.new(targetPosition.X, -40, targetPosition.Z)
        end
        if index == 3 then
            return Vector3.new(targetPosition.X, targetPosition.Y + 5, targetPosition.Z)
        end
        return nil
    end
    self:SetTweenNoclipEnabled(true)
    return self:AdvanceTweenStage()
end

function UI:GetPlayerTeleportOptions()
    local options = {"Select Player"}
    for _, player in ipairs(Players:GetPlayers()) do
        if player ~= LocalPlayer then
            options[#options + 1] = player.Name
        end
    end
    table.sort(options, function(a, b)
        if a == "Select Player" then
            return true
        end
        if b == "Select Player" then
            return false
        end
        return string.lower(a) < string.lower(b)
    end)
    return options
end

function UI:RefreshPlayerTeleportOptions()
    local control = self:GetControl(3, "player_tp")
    if not control then
        return
    end

    control.options = self:GetPlayerTeleportOptions()
    local stillValid = false
    for _, option in ipairs(control.options) do
        if option == control.value then
            stillValid = true
            break
        end
    end
    if not stillValid then
        control.value = "Select Player"
    end
end

function UI:TweenToPlayer(playerName)
    if not playerName or playerName == "Select Player" then
        TweenDebug("TweenToPlayer skipped: no player selected")
        return
    end

    local targetPlayer = Players:FindFirstChild(playerName)
    local localRootPart = self:GetLocalRootPart()
    if not targetPlayer or not localRootPart then
        return
    end

    local function getLiveTargetPosition()
        local targetCharacter = targetPlayer and targetPlayer.Character
        local targetRootPart = self:GetCharacterTeleportPart(targetCharacter)
        return targetRootPart and targetRootPart.Position or nil
    end

    local initialTargetPosition = getLiveTargetPosition()
    if not initialTargetPosition then
        return
    end

    local canTween, distanceToTarget = self:CanTweenToTarget(initialTargetPosition, true)
    if not canTween then
        TweenDebug("TweenToPlayer skipped: local player not on car")
        return
    end

    self:CancelTweenToPositions("restart")
    local tweenId = (self.activeTweenId or 0) + 1
    self.activeTweenId = tweenId
    self.tweenState.active = true
    self.tweenState.tweenId = tweenId
    self.tweenState.totalStages = distanceToTarget and distanceToTarget < 50 and 1 or 3
    self.tweenState.stageIndex = 0
    self.tweenState.speed = self:GetTweenSpeed() * TWEEN_SPEED_SCALE
    self.tweenState.stageProvider = function(index, liveRootPart)
        local targetPosition = getLiveTargetPosition()
        if not targetPosition then
            return nil
        end
        if distanceToTarget and distanceToTarget < 50 then
            if index == 1 then
                return targetPosition
            end
            return nil
        end
        if index == 1 then
            return Vector3.new(liveRootPart.Position.X, -40, liveRootPart.Position.Z)
        end
        if index == 2 then
            return Vector3.new(targetPosition.X, -40, targetPosition.Z)
        end
        if index == 3 then
            return Vector3.new(targetPosition.X, targetPosition.Y + 5, targetPosition.Z)
        end
        return nil
    end
    self:SetTweenNoclipEnabled(true)
    self:AdvanceTweenStage()
end

function UI:FormatDuration(seconds)
    local total = math.max(0, math.floor(tonumber(seconds) or 0))
    local hours = math.floor(total / 3600)
    local minutes = math.floor((total % 3600) / 60)
    local secs = total % 60

    if hours > 0 then
        return string.format("%dh %dm %ds", hours, minutes, secs)
    end

    if minutes > 0 then
        return string.format("%dm %ds", minutes, secs)
    end

    return string.format("%ds", secs)
end

function UI:RefreshHomeStats(force)
    local now = tick()
    if not force and self.selectedTab ~= 1 then
        return
    end

    if not force and (now - (self.lastHomeStatRefresh or 0)) < 1 then
        return
    end

    self.lastHomeStatRefresh = now
    self:SetStatValue("uptime_stat", self:FormatDuration(now - (self.launchTick or now)))
    self:SetStatValue("profile_stat", "Undetected")
    self:SetStatValue("farm_state_stat", tostring(self.farmState.status or "Idle"))
    self:SetStatValue("on_car_stat", self:IsLocalPlayerOnCar() and "True" or "False")
    if self.lastConfigSaveTick then
        self:SetStatValue("last_save_stat", self:FormatDuration(now - self.lastConfigSaveTick) .. " ago")
    else
        self:SetStatValue("last_save_stat", "Never")
    end
end

function UI:ApplyAccentTheme(themeName)
    local palette = AccentThemes[tostring(themeName or "")] or AccentThemes["Green"]
    Theme.accent = palette.accent
    Theme.accentSoft = palette.accentSoft
end

function UI:UpdateRainbowAccent()
    local accentControl = self:GetControl(self.settingsTabIndex or 4, "accent_theme")
    if not accentControl or accentControl.value ~= "Rainbow" then
        return
    end

    local hue = (tick() * 0.12) % 1
    local accent = Color3.fromHSV(hue, 0.72, 1)
    local accentSoft = Color3.fromHSV(hue, 0.62, 0.78)

    local previousAccent = Theme.accent
    local previousAccentSoft = Theme.accentSoft
    Theme.accent = accent
    Theme.accentSoft = accentSoft

    local accentChanged = not previousAccent
        or math.abs(previousAccent.R - accent.R) > 0.01
        or math.abs(previousAccent.G - accent.G) > 0.01
        or math.abs(previousAccent.B - accent.B) > 0.01
    local accentSoftChanged = not previousAccentSoft
        or math.abs(previousAccentSoft.R - accentSoft.R) > 0.01
        or math.abs(previousAccentSoft.G - accentSoft.G) > 0.01
        or math.abs(previousAccentSoft.B - accentSoft.B) > 0.01

    self:MarkShellDirty()
    self:MarkCurrentTabDirty()
    self.needsRedraw = true
end

function UI:GetConfigFolderPath()
    return self.configFolderName or "WindyBase"
end

function UI:GetConfigFilePath()
    return self:GetConfigFolderPath() .. "/" .. (self.configFileName or "config.json")
end

function UI:EnsureConfigStorage()
    if type(isfolder) == "function" then
        local ok, exists = pcall(isfolder, self:GetConfigFolderPath())
        if ok and exists then
            return true
        end
    end

    if type(makefolder) == "function" then
        local ok = pcall(makefolder, self:GetConfigFolderPath())
        if ok then
            return true
        end
    end

    return false
end

function UI:IsAutoSaveConfigEnabled()
    local control = self:GetControl(4, "auto_save_config")
    return control and control.value == true
end

function UI:AreAnimationsEnabled()
    local control = self:GetControl(4, "ui_animations")
    return not control or control.value == true
end

function UI:BuildConfigData()
    local data = {
        version = 1,
        controls = {},
    }

    for tabIndex, tabControls in ipairs(self.controls or {}) do
        for _, control in ipairs(tabControls) do
            if control.type ~= "button" and control.type ~= "stat" then
                local entry = {
                    tab = tabIndex,
                    type = control.type,
                }

                if control.type == "dropdown_multi" then
                    entry.selected = control.selected or {}
                else
                    entry.value = control.value
                end

                data.controls[control.id] = entry
            end
        end
    end

    return data
end

function UI:ApplyConfigData(data)
    if type(data) ~= "table" or type(data.controls) ~= "table" then
        return
    end

    for controlId, saved in pairs(data.controls) do
        if type(saved) == "table" then
            local tabIndex = tonumber(saved.tab)
            local control = tabIndex and self:GetControl(tabIndex, controlId) or nil
            if control then
                if control.type == "dropdown_multi" and type(saved.selected) == "table" then
                    local applied = {}
                    for option, isSelected in pairs(saved.selected) do
                        applied[tostring(option)] = isSelected == true
                    end
                    control.selected = applied
                elseif saved.value ~= nil then
                    control.value = saved.value
                end
            end
        end
    end

    local widthControl = self:GetControl(4, "ui_scale")
    if widthControl and widthControl.value then
        self.window.width = widthControl.value
    end

    local heightControl = self:GetControl(4, "ui_height")
    if heightControl and heightControl.value then
        self.window.height = heightControl.value
    end

    local accentControl = self:GetControl(4, "accent_theme")
    if accentControl then
        self:ApplyAccentTheme(accentControl.value)
    end

    self:SanitizePremiumControls()

    self:MarkShellDirty()
    for tabIndex = 1, #self.tabDirty do
        self:MarkTabDirty(tabIndex)
    end
end

function UI:SaveConfig(force)
    if not force and not self:IsAutoSaveConfigEnabled() then
        return false
    end

    if type(writefile) ~= "function" or not HttpService then
        return false
    end

    self:EnsureConfigStorage()

    local encodeOk, payload = pcall(function()
        return HttpService:JSONEncode(self:BuildConfigData())
    end)
    if not encodeOk or not payload then
        return false
    end

    local writeOk = pcall(writefile, self:GetConfigFilePath(), payload)
    if writeOk then
        self.lastConfigSaveTick = tick()
        self:SetStatValue("last_save_stat", "0s ago")
    end
    return writeOk
end

function UI:LoadConfig()
    if type(readfile) ~= "function" or type(isfile) ~= "function" or not HttpService then
        return false
    end

    self:EnsureConfigStorage()

    local existsOk, exists = pcall(isfile, self:GetConfigFilePath())
    if not existsOk or not exists then
        return false
    end

    local readOk, payload = pcall(readfile, self:GetConfigFilePath())
    if not readOk or not payload or payload == "" then
        return false
    end

    local decodeOk, data = pcall(function()
        return HttpService:JSONDecode(payload)
    end)
    if not decodeOk or type(data) ~= "table" then
        return false
    end

    self:ApplyConfigData(data)
    return true
end

function UI:NotifyPlayer(message, title, duration)
    if type(notify) == "function" then
        pcall(notify, tostring(message or ""), tostring(title or "Windy"), tonumber(duration) or 3)
    end
end

function UI:SetFarmStatus(statusText)
    self.farmState.status = tostring(statusText or "Idle")
end

function UI:CanTweenToTarget(targetPosition, notifyOnFailure)
    local rootPart = self:GetLocalRootPart()
    if not rootPart or typeof(targetPosition) ~= "Vector3" then
        return false, nil
    end

    local distance = (rootPart.Position - targetPosition).Magnitude
    if distance > 50 and not self:IsLocalPlayerOnCar() then
        if notifyOnFailure then
            self:NotifyPlayer("You need a car", "Bloxburg", 3)
        end
        return false, distance
    end

    return true, distance
end

function UI:IsFarmEnabled()
    local control = self:GetControl(2, "farm_toggle")
    return control and control.value == true
end

function UI:ResetFarmState()
    self.farmState.pizzaMountStage = nil
    self.farmState.taxiMountStage = nil
    self.farmState.bffStage = nil
    self.farmState.bffPathIndex = 0
    self.farmState.bffSearchTarget = nil
    self.farmState.bffLastPosition = nil
    self.farmState.bffLastMoveTick = 0
    self.farmState.lastFarNotifyAt = 0
    self.farmState.lastWrongCarNotifyAt = 0
    self.farmState.lastPickupInteractAt = 0
    self.farmState.lastDeliveryInteractAt = 0
    self.farmState.lastFarmNotifyAt = 0
    self.farmState.lastTaxiInteractAt = 0
    self.farmState.lastTaxiWaitStartedAt = 0
    self.farmState.lastTaxiZoneWaitStartedAt = 0
    self.farmState.lastBffInteractAt = 0
    self.farmState.status = "Idle"
    self:StopWalking()
    self:ClearPathNodes()
end

function UI:PanicReset()
    self:CancelTweenToPositions("panic_reset")
    self:HideEspPrefix("debug_")
    self:HidePathDebugDrawings()
    self:ResetFarmState()
end

function UI:GetTaxiStandPart()
    local environment = workspace and workspace:FindFirstChild("Environment")
    local taxiStands = environment and environment:FindFirstChild("TaxiStands")
    local taxiFolder = taxiStands and taxiStands:FindFirstChild("Taxi")
    local taxiModel = taxiFolder and taxiFolder:FindFirstChild("TruFleet City Taxi")
    local vehicle = taxiModel and taxiModel:FindFirstChild("Vehicle")
    local chassi = vehicle and vehicle:FindFirstChild("Chassi")
    if chassi and chassi:IsA("BasePart") then
        return chassi
    end
    return nil
end

function UI:GetNearestTaxiStandPart()
    local environment = workspace and workspace:FindFirstChild("Environment")
    local taxiStands = environment and environment:FindFirstChild("TaxiStands")
    local taxiFolder = taxiStands and taxiStands:FindFirstChild("Taxi")
    if not taxiFolder then
        return nil
    end

    local bestPart = nil
    local bestDistance = math.huge

    for _, descendant in ipairs(taxiFolder:GetDescendants()) do
        if descendant:IsA("BasePart") and descendant.Name == "Chassi" then
            local model = descendant.Parent and descendant.Parent.Parent
            local isTaxi = model and model.Name == "TruFleet City Taxi"
            if isTaxi then
                local distance = (descendant.Position - TAXI_JOB_POSITION).Magnitude
                if distance < bestDistance then
                    bestDistance = distance
                    bestPart = descendant
                end
            end
        end
    end

    return bestPart
end

function UI:IsOnTaxiVehicle()
    local vehicleModel = self:GetLocalVehicleModel()
    return vehicleModel and vehicleModel.Name == "Vehicle_TruFleet City Taxi" or false
end

function UI:GetTaxiCustomerPosition()
    local gameFolder = workspace:FindFirstChild("_game")
    local spawnedCharacters = gameFolder and gameFolder:FindFirstChild("SpawnedCharacters")
    local customer = spawnedCharacters and spawnedCharacters:FindFirstChild("TaxiCustomer")
    if not customer then
        return nil
    end

    local ownerMarker = customer:FindFirstChild("plr")
    if ownerMarker then
        local ownerName = nil
        local ok = pcall(function()
            ownerName = ownerMarker.Value
        end)
        if (not ok or ownerName == nil) and ownerMarker:IsA("TextLabel") then
            ownerName = ownerMarker.Text
        end
        if tostring(ownerName or "") ~= tostring(LocalPlayer and LocalPlayer.Name or "") then
            return nil
        end
    end

    local rootPart = customer:FindFirstChild("HumanoidRootPart")
    if rootPart and rootPart:IsA("BasePart") then
        return rootPart.Position
    end

    local head = customer:FindFirstChild("Head")
    if head and head:IsA("BasePart") then
        return head.Position
    end

    return nil
end

function UI:HasTaxiZoneBox()
    local taxiZone = workspace:FindFirstChild("TaxiZone")
    return taxiZone and taxiZone:FindFirstChild("Box") ~= nil
end

function UI:IsPremiumUser()
    return PREMIUM_USERS[tostring(LocalPlayer and LocalPlayer.Name or "")] == true
end

function UI:GetVersionStatusText()
    if self:IsPremiumUser() then
        return "Premium"
    end
    return "Free Version"
end

function UI:SanitizePremiumControls()
    if self:CanUsePremiumFeature() then
        return false
    end

    local changed = false
    local carFlyControl = self:GetControl(self.premiumTabIndex or 6, "car_fly_toggle")
    if carFlyControl and carFlyControl.value == true then
        carFlyControl.value = false
        changed = true
    end

    local carSpeedControl = self:GetControl(self.premiumTabIndex or 6, "car_speed_toggle")
    if carSpeedControl and carSpeedControl.value == true then
        carSpeedControl.value = false
        changed = true
    end

    local jobControl = self:GetControl(2, "job_selection")
    if jobControl and self:IsPremiumJob(jobControl.value) then
        jobControl.value = "Select Job"
        changed = true
    end

    self.carFlyEnabled = false
    self.carFlyActive = false
    self.carFlyPosition = nil

    return changed
end

function UI:GetSelectedJob()
    local control = self:GetControl(2, "job_selection")
    local jobName = tostring(control and control.value or "Select Job")
    if self:IsPremiumJob(jobName) and not self:CanUsePremiumFeature() then
        return "Select Job"
    end
    return jobName
end

function UI:IsPremiumJob(jobName)
    return PREMIUM_JOBS[tostring(jobName or "")] == true
end

function UI:CanUsePremiumFeature()
    return self:IsPremiumUser()
end

function UI:TryMountPizzaBike()
    local rootPart = self:GetLocalRootPart()
    local bikePart = self:GetPizzaBikeEspPart()
    if not rootPart or not bikePart then
        return false
    end

    if (rootPart.Position - PIZZA_PICKUP_POSITION).Magnitude > 30 then
        self:SetFarmStatus("Rehiring")
        if (tick() - (self.farmState.lastFarNotifyAt or 0)) >= 2 then
            self.farmState.lastFarNotifyAt = tick()
            self:NotifyPlayer("Not close enough or get close to job site", "Bloxburg", 3)
        end
        self.farmState.pizzaMountStage = nil
        return false
    end

    if self.tweenState.active then
        return false
    end

    local mountStage = self.farmState.pizzaMountStage or "pickup"

    if mountStage == "pickup" then
        self:SetFarmStatus("Rehiring")
        if (rootPart.Position - PIZZA_PICKUP_POSITION).Magnitude > 8 then
            self:TweenWithVehicleTo(PIZZA_PICKUP_POSITION)
            return false
        end
        self.farmState.pizzaMountStage = "bike"
        return false
    end

    if mountStage == "bike" then
        self:SetFarmStatus("Getting bike")
        if (rootPart.Position - bikePart.Position).Magnitude > 8 then
            self:TweenWithVehicleTo(bikePart.Position)
            return false
        end
        self.farmState.pizzaMountStage = "mount"
        return false
    end

    if mountStage == "mount" then
        self:SetFarmStatus("Getting bike")
        self:PressInteractKey()
        if self:IsOnPizzaDeliveryMoped() then
            self.farmState.pizzaMountStage = nil
            return true
        end
        return false
    end

    self.farmState.pizzaMountStage = "pickup"
    if (rootPart.Position - PIZZA_PICKUP_POSITION).Magnitude > 8 then
        self:TweenWithVehicleTo(PIZZA_PICKUP_POSITION)
        return false
    end
    return false
end

function UI:TryMountTaxi()
    local standPart = self:GetNearestTaxiStandPart() or self:GetTaxiStandPart()
    local rootPart = self:GetLocalRootPart()
    if not rootPart then
        TaxiDebug("Taxi mount: local root part missing")
        return false
    end

    if not standPart then
        TaxiDebug("Taxi mount: nearest taxi stand part missing")
        return false
    end

    local jobDistance = (rootPart.Position - TAXI_JOB_POSITION).Magnitude
    local standDistance = (rootPart.Position - standPart.Position).Magnitude
    local mountStage = self.farmState.taxiMountStage or "job"

    TaxiDebug(string.format(
        "Taxi mount: stage=%s jobDistance=%.2f standDistance=%.2f tweenActive=%s",
        tostring(mountStage),
        jobDistance,
        standDistance,
        tostring(self.tweenState.active == true)
    ))

    if jobDistance > 50 then
        if (tick() - (self.farmState.lastFarNotifyAt or 0)) >= 2 then
            self.farmState.lastFarNotifyAt = tick()
            self:NotifyPlayer("Not close enough or get close to job site", "Bloxburg", 3)
        end
        self.farmState.taxiMountStage = nil
        return false
    end

    if self.tweenState.active then
        TaxiDebug("Taxi mount: tween active, waiting")
        return false
    end

    self:SetFarmStatus("Getting bike")

    if mountStage == "job" then
        if jobDistance > 8 then
            TaxiDebug(string.format(
                "Taxi mount: tweening to job point target=(%.2f, %.2f, %.2f)",
                TAXI_JOB_POSITION.X,
                TAXI_JOB_POSITION.Y,
                TAXI_JOB_POSITION.Z
            ))
            self:TweenWithVehicleTo(TAXI_JOB_POSITION)
            return false
        end
        self.farmState.taxiMountStage = "stand"
        TaxiDebug("Taxi mount: reached job point, advancing to stand")
        return false
    end

    if mountStage == "stand" then
        if standDistance > 8 then
            TaxiDebug(string.format(
                "Taxi mount: tweening to nearest stand target=(%.2f, %.2f, %.2f)",
                standPart.Position.X,
                standPart.Position.Y,
                standPart.Position.Z
            ))
            self:TweenWithVehicleTo(standPart.Position)
            return false
        end
        self.farmState.taxiMountStage = "mount"
        TaxiDebug("Taxi mount: reached stand, advancing to mount")
        return false
    end

    if mountStage == "mount" then
        if (tick() - (self.farmState.lastTaxiInteractAt or 0)) >= 0.35 then
            self.farmState.lastTaxiInteractAt = tick()
            TaxiDebug("Taxi mount: pressing E to enter taxi")
            self:PressInteractKey()
        end

        if self:IsOnTaxiVehicle() then
            TaxiDebug("Taxi mount: taxi acquired")
            self.farmState.taxiMountStage = nil
            return true
        end

        return false
    end

    self.farmState.taxiMountStage = "job"
    return false
end

function UI:HandlePizzaDeliveryFarm()
    self:ClearPathNodes()
    if self:GetSelectedJob() ~= "Pizza Delivery Job" then
        FarmDebug("Farm skipped: unsupported job selection")
        return
    end

    if not self:IsLocalPlayerOnCar() then
        self:TryMountPizzaBike()
        return
    end

    if not self:IsOnPizzaDeliveryMoped() then
        if (tick() - (self.farmState.lastWrongCarNotifyAt or 0)) >= 2 then
            self.farmState.lastWrongCarNotifyAt = tick()
            self:NotifyPlayer("Wrong car: Vehicle_Delivery Moped required", "Bloxburg", 3)
        end
        return
    end

    if self.tweenState.active then
        return
    end

    if not self:GetLocalPizzaBox() then
        local pickupPosition = PIZZA_PICKUP_POSITION
        local rootPart = self:GetLocalRootPart()
        if not rootPart then
            FarmDebug("Farm pickup aborted: no local root part")
            return
        end

        local pickupDistance = (rootPart.Position - pickupPosition).Magnitude
        FarmDebug(string.format("Farm pickup branch: hasPizza=false distance=%.2f", pickupDistance))

        if pickupDistance > 12 then
            self:SetFarmStatus("Rehiring")
            FarmDebug("Farm pickup: tweening to pickup point")
            self:TweenWithVehicleTo(pickupPosition)
            return
        end

        self:SetFarmStatus("Rehiring")
        self:HoldInteractionTarget(pickupPosition)
        if (tick() - (self.farmState.lastPickupInteractAt or 0)) >= 0.35 then
            self.farmState.lastPickupInteractAt = tick()
            self:PressInteractKey()
        end
        return
    end

    local customerPosition = self:GetCustomerTargetPosition()
    if not customerPosition then
        if (tick() - (self.farmState.lastFarmNotifyAt or 0)) >= 2 then
            self.farmState.lastFarmNotifyAt = tick()
            FarmDebug("Farm delivery skipped: customer position missing")
        end
        return
    end

    FarmDebug(string.format(
        "Farm delivery branch: customer target=(%.2f, %.2f, %.2f)",
        customerPosition.X,
        customerPosition.Y,
        customerPosition.Z
    ))

    local rootPart = self:GetLocalRootPart()
    if not rootPart then
        return
    end

    local deliveryDistance = (rootPart.Position - customerPosition).Magnitude
    if deliveryDistance > 12 then
        self:SetFarmStatus("Going customer")
        self:TweenWithVehicleTo(customerPosition)
        return
    end

    self:HoldInteractionTarget(customerPosition)
    if (tick() - (self.farmState.lastDeliveryInteractAt or 0)) >= 0.35 then
        self:SetFarmStatus("Delivering")
        self.farmState.lastDeliveryInteractAt = tick()
        self:PressInteractKey()
    end
end

function UI:HandleTaxiFarm()
    self:ClearPathNodes()
    if not self:IsLocalPlayerOnCar() then
        TaxiDebug("Taxi farm: not on car, trying to mount taxi")
        self:TryMountTaxi()
        return
    end

    if not self:IsOnTaxiVehicle() then
        if (tick() - (self.farmState.lastWrongCarNotifyAt or 0)) >= 2 then
            self.farmState.lastWrongCarNotifyAt = tick()
            self:NotifyPlayer("Wrong car: Vehicle_TruFleet City Taxi required", "Bloxburg", 3)
        end
        return
    end

    if self.tweenState.active then
        TaxiDebug("Taxi farm: tween active, waiting")
        return
    end

    local rootPart = self:GetLocalRootPart()
    if not rootPart then
        return
    end

    local customerPosition = self:GetTaxiCustomerPosition()
    if not customerPosition then
        self:SetFarmStatus("Waiting customer")
        self.farmState.lastTaxiWaitStartedAt = 0
        self.farmState.lastTaxiZoneWaitStartedAt = 0
        local standPart = self:GetTaxiStandPart()
        if standPart then
            local standDistance = (rootPart.Position - standPart.Position).Magnitude
            if standDistance > 15 then
                TaxiDebug(string.format(
                    "Taxi farm: no customer found, returning to stand target=(%.2f, %.2f, %.2f) distance=%.2f",
                    standPart.Position.X,
                    standPart.Position.Y,
                    standPart.Position.Z,
                    standDistance
                ))
                self:TweenWithVehicleTo(standPart.Position)
                return
            end
        end
        TaxiDebug("Taxi farm: no customer found, waiting at stand")
        return
    end

    local customerDistance = (rootPart.Position - customerPosition).Magnitude
    if customerDistance > 15 then
        self:SetFarmStatus("Going customer")
        self.farmState.lastTaxiWaitStartedAt = 0
        self.farmState.lastTaxiZoneWaitStartedAt = 0
        TaxiDebug(string.format(
            "Taxi farm: going to customer target=(%.2f, %.2f, %.2f) distance=%.2f",
            customerPosition.X,
            customerPosition.Y,
            customerPosition.Z,
            customerDistance
        ))
        self:TweenWithVehicleTo(customerPosition)
        return
    end

    if (self.farmState.lastTaxiWaitStartedAt or 0) == 0 then
        self.farmState.lastTaxiWaitStartedAt = tick()
        self:SetFarmStatus("Waiting customer")
        TaxiDebug("Taxi farm: reached customer, starting 4s wait")
        return
    end

    if (tick() - (self.farmState.lastTaxiWaitStartedAt or 0)) < 4 then
        self:SetFarmStatus("Waiting customer")
        TaxiDebug(string.format(
            "Taxi farm: waiting with customer elapsed=%.2f",
            tick() - (self.farmState.lastTaxiWaitStartedAt or 0)
        ))
        return
    end

    if self:HasTaxiZoneBox() then
        local taxiZone = workspace:FindFirstChild("TaxiZone")
        local box = taxiZone and taxiZone:FindFirstChild("Box")
        if box and box:IsA("BasePart") then
            local zoneDistance = (rootPart.Position - box.Position).Magnitude
            if zoneDistance > 15 then
                self:SetFarmStatus("Delivering")
                self.farmState.lastTaxiZoneWaitStartedAt = 0
                TaxiDebug(string.format(
                    "Taxi farm: going to taxi zone box target=(%.2f, %.2f, %.2f) distance=%.2f",
                    box.Position.X,
                    box.Position.Y,
                    box.Position.Z,
                    zoneDistance
                ))
                self:TweenWithVehicleTo(box.Position)
                return
            end

            if (self.farmState.lastTaxiZoneWaitStartedAt or 0) == 0 then
                self.farmState.lastTaxiZoneWaitStartedAt = tick()
                TaxiDebug("Taxi farm: reached taxi zone box, waiting for customer despawn")
            end

            self:SetFarmStatus("Delivering")
            if not self:GetTaxiCustomerPosition() or (tick() - (self.farmState.lastTaxiZoneWaitStartedAt or 0)) >= 10 then
                self.farmState.lastTaxiWaitStartedAt = 0
                self.farmState.lastTaxiZoneWaitStartedAt = 0
                self:SetFarmStatus("Waiting customer")
                TaxiDebug("Taxi farm: dropoff complete, resetting for next customer scan")
                return
            end
            TaxiDebug(string.format(
                "Taxi farm: waiting at taxi zone elapsed=%.2f customerPresent=%s",
                tick() - (self.farmState.lastTaxiZoneWaitStartedAt or 0),
                tostring(self:GetTaxiCustomerPosition() ~= nil)
            ))
            return
        end
    end

    self:SetFarmStatus("Waiting customer")
    TaxiDebug("Taxi farm: taxi zone box missing, waiting for next customer")
end

function UI:HandleBffSupermarketFarm()
    local rootPart = self:GetLocalRootPart()
    if not rootPart then
        return
    end

    local now = tick()
    local lastPosition = self.farmState.bffLastPosition
    if typeof(lastPosition) ~= "Vector3" then
        self.farmState.bffLastPosition = rootPart.Position
        self.farmState.bffLastMoveTick = now
    else
        local movementDistance = (rootPart.Position - lastPosition).Magnitude
        if movementDistance > 0.35 then
            self.farmState.bffLastPosition = rootPart.Position
            self.farmState.bffLastMoveTick = now
        end
    end

    local stage = self.farmState.bffStage or "job"
    if stage == "return" then
        self:SetPathNodes(BFF_RETURN_PATH)
    else
        self:SetPathNodes(BFF_PATH)
    end

    local jobDistance = (rootPart.Position - BFF_JOB_POSITION).Magnitude
    if stage == "job" and jobDistance > 30 then
        self:SetFarmStatus("Idle")
        self:StopWalking()
        self.farmState.bffStage = nil
        self.farmState.bffPathIndex = 0
        if (tick() - (self.farmState.lastFarNotifyAt or 0)) >= 2 then
            self.farmState.lastFarNotifyAt = tick()
            self:NotifyPlayer("Not close enough or get close to job site", "Bloxburg", 3)
        end
        return
    end

    if self.tweenState.active then
        self:SetFarmStatus("Walking")
        return
    end

    if stage == "job" then
        if jobDistance > 8 then
            self:SetFarmStatus("Walking")
            self:TweenToPositions({BFF_JOB_POSITION})
            return
        end
        self.farmState.bffStage = "search"
        self.farmState.bffSearchTarget = nil
        stage = "search"
    end

    if stage == "search" then
        if self:GetLocalFoodCrate() then
            self:StopWalking()
            self.farmState.bffStage = "path"
            self.farmState.bffPathIndex = 1
            self.farmState.bffSearchTarget = nil
            self.farmState.bffLastPosition = rootPart.Position
            self.farmState.bffLastMoveTick = now
            return
        end

        if (now - (self.farmState.lastBffInteractAt or 0)) >= 0.5 then
            self.farmState.lastBffInteractAt = now
            self:PressInteractKey()
        end

        local searchTarget = self.farmState.bffSearchTarget
        if typeof(searchTarget) ~= "Vector3" or (rootPart.Position - searchTarget).Magnitude <= WALK_TARGET_THRESHOLD then
            searchTarget = self:GetRandomBffCrateSearchPosition()
            self.farmState.bffSearchTarget = searchTarget
            self:StopWalking()
        end

        self:SetFarmStatus("Walking")
        if self:IsWalking() and (now - (self.farmState.bffLastMoveTick or now)) >= 2 then
            self:StopWalking()
            self:TweenToPositions({searchTarget})
            self.farmState.bffLastPosition = rootPart.Position
            self.farmState.bffLastMoveTick = now
            return
        end
        if not self:IsWalking() or not WalkToService.currentTarget or (WalkToService.currentTarget - searchTarget).Magnitude > 1 then
            self:WalkTo(searchTarget)
        end
        return
    end

    if stage == "path" then
        if (now - (self.farmState.lastBffInteractAt or 0)) >= 0.5 then
            self.farmState.lastBffInteractAt = now
            self:PressInteractKey()
        end

        if not self:GetLocalFoodCrate() then
            local shortcutReturnIndex = self:GetClosestBffReturnIndex(rootPart.Position, 8)
            if shortcutReturnIndex then
                self:StopWalking()
                self.farmState.bffStage = "return"
                self.farmState.bffPathIndex = shortcutReturnIndex
                return
            end
        end

        self.farmState.bffStage = "path"
        local pathIndex = tonumber(self.farmState.bffPathIndex) or 1
        local nodeEntry = BFF_PATH[pathIndex]
        if not nodeEntry or typeof(nodeEntry.position) ~= "Vector3" then
            if self:GetLocalFoodCrate() then
                self.farmState.bffPathIndex = 1
            else
                self:StopWalking()
                self.farmState.bffStage = "return"
                self.farmState.bffPathIndex = 1
            end
            return
        end

        local nodeDistance = (rootPart.Position - nodeEntry.position).Magnitude
        if nodeDistance <= WALK_TARGET_THRESHOLD then
            local nextIndex = pathIndex + 1
            local nextNode = BFF_PATH[nextIndex]
            if nextNode and typeof(nextNode.position) == "Vector3" then
                self.farmState.bffPathIndex = nextIndex
                self:WalkTo(nextNode.position)
                self.farmState.bffLastPosition = rootPart.Position
                self.farmState.bffLastMoveTick = now
            else
                if self:GetLocalFoodCrate() then
                    self.farmState.bffPathIndex = 1
                    self:WalkTo(BFF_PATH[1].position)
                    self.farmState.bffLastPosition = rootPart.Position
                    self.farmState.bffLastMoveTick = now
                else
                    self:StopWalking()
                    self.farmState.bffStage = "return"
                    self.farmState.bffPathIndex = 1
                end
            end
            return
        end

        self:SetFarmStatus("Walking")
        if self:IsWalking() and (now - (self.farmState.bffLastMoveTick or now)) >= 2 then
            self:StopWalking()
            self:TweenToPositions({nodeEntry.position})
            self.farmState.bffLastPosition = rootPart.Position
            self.farmState.bffLastMoveTick = now
            return
        end
        if not self:IsWalking() or not WalkToService.currentTarget or (WalkToService.currentTarget - nodeEntry.position).Magnitude > 1 then
            self:WalkTo(nodeEntry.position)
        end
        return
    end

    if stage == "return" then
        if (now - (self.farmState.lastBffInteractAt or 0)) >= 0.5 then
            self.farmState.lastBffInteractAt = now
            self:PressInteractKey()
        end

        local returnIndex = tonumber(self.farmState.bffPathIndex) or 1
        local returnNode = BFF_RETURN_PATH[returnIndex]
        if not returnNode or typeof(returnNode.position) ~= "Vector3" then
            self:StopWalking()
            self.farmState.bffStage = "job"
            self.farmState.bffPathIndex = 0
            self.farmState.bffSearchTarget = nil
            self:SetFarmStatus("Walking")
            return
        end

        local returnDistance = (rootPart.Position - returnNode.position).Magnitude
        if returnDistance <= WALK_TARGET_THRESHOLD then
            local nextIndex = returnIndex + 1
            local nextNode = BFF_RETURN_PATH[nextIndex]
            if nextNode and typeof(nextNode.position) == "Vector3" then
                self.farmState.bffPathIndex = nextIndex
                self:WalkTo(nextNode.position)
                self.farmState.bffLastPosition = rootPart.Position
                self.farmState.bffLastMoveTick = now
            else
                self:StopWalking()
                self.farmState.bffStage = "job"
                self.farmState.bffPathIndex = 0
                self.farmState.bffSearchTarget = nil
                self:SetFarmStatus("Walking")
            end
            return
        end

        self:SetFarmStatus("Walking")
        if self:IsWalking() and (now - (self.farmState.bffLastMoveTick or now)) >= 2 then
            self:StopWalking()
            self:TweenToPositions({returnNode.position})
            self.farmState.bffLastPosition = rootPart.Position
            self.farmState.bffLastMoveTick = now
            return
        end
        if not self:IsWalking() or not WalkToService.currentTarget or (WalkToService.currentTarget - returnNode.position).Magnitude > 1 then
            self:WalkTo(returnNode.position)
        end
        return
    end

    self:StopWalking()
    self:SetFarmStatus("Waiting customer")
end

function UI:EnsureFarmWorker()
    if self.farmWorkerRunning then
        return
    end

    self.farmWorkerRunning = true
    task.spawn(function()
        while self.isRunning do
            if self:IsFarmEnabled() then
                if self:GetSelectedJob() == "Pizza Delivery Job" then
                    self:HandlePizzaDeliveryFarm()
                elseif self:GetSelectedJob() == "Bloxburg Taxi" then
                    self:HandleTaxiFarm()
                elseif self:GetSelectedJob() == "BFF Supermarket" then
                    self:HandleBffSupermarketFarm()
                end
            else
                self:ResetFarmState()
            end
            task.wait(0.25)
        end
        self.farmWorkerRunning = false
    end)
end

function UI:GetContentBounds()
    local renderX, renderY = self:GetRenderWindowPosition()
    local x = renderX + self.window.sidebarWidth + 14
    local y = renderY + 16 + self:GetAnimatedWindowYOffset()
    local width = self.window.width - self.window.sidebarWidth - 28
    local height = self.window.height - 32
    return x, y, width, height
end

function UI:GetScrollbarMetrics()
    local x, y, width, height = self:GetContentBounds()
    local scrollbarX = x + width - 10
    local scrollbarY = y + 8
    local scrollbarW = 4
    local scrollbarH = height - 16
    local thumbH = 0
    local thumbY = scrollbarY

    if self.scroll.max > 0 then
        thumbH = math.max(34, math.floor(scrollbarH * (height / (height + self.scroll.max))))
        local available = math.max(1, scrollbarH - thumbH)
        thumbY = scrollbarY + math.floor((self.scroll.offset / self.scroll.max) * available)
    end

    return scrollbarX, scrollbarY, scrollbarW, scrollbarH, thumbY, thumbH
end

function UI:UpdateScrollLimit()
    local previousMax = self.scroll.max
    local previousOffset = self.scroll.offset
    local _, _, _, viewportHeight = self:GetContentBounds()
    local contentHeight = 28

    for _, control in ipairs(self:GetActiveControls()) do
        contentHeight = contentHeight + ((control.type == "stat") and 54 or 98)
        if self.expandedDropdown == control.id and control.options then
            contentHeight = contentHeight + (#control.options * 30) + 10
        end
    end

    self.scroll.max = math.max(0, contentHeight - viewportHeight + 12)
    self.scroll.offset = math.clamp(self.scroll.offset, 0, self.scroll.max)
    return previousMax ~= self.scroll.max or previousOffset ~= self.scroll.offset
end

function UI:MarkShellDirty()
    if not self.shellDirty then
        self.shellDirty = true
    end
    if not self.needsRedraw then
        self.needsRedraw = true
    end
end

function UI:MarkTabDirty(tabIndex)
    if not self.tabDirty[tabIndex] then
        self.tabDirty[tabIndex] = true
    end
    if not self.needsRedraw then
        self.needsRedraw = true
    end
end

function UI:MarkCurrentTabDirty()
    self:MarkTabDirty(self.selectedTab)
end

function UI:SetScrollOffset(offset)
    local nextOffset = math.clamp(math.floor(offset), 0, self.scroll.max)
    if self.scroll.offset == nextOffset then
        return false
    end

    self.scroll.offset = nextOffset
    self:MarkCurrentTabDirty()
    return true
end

function UI:ToggleDropdown(controlId)
    local previousExpanded = self.expandedDropdown
    if self.expandedDropdown == controlId then
        self.expandedDropdown = nil
    else
        self.expandedDropdown = controlId
    end
    if previousExpanded ~= self.expandedDropdown then
        self:UpdateScrollLimit()
        self:MarkCurrentTabDirty()
    end
end

function UI:GetSliderPercent(control)
    local minValue = control.min or 0
    local maxValue = control.max or 1
    local value = control.value or minValue
    if maxValue <= minValue then
        return 0
    end

    return math.clamp((value - minValue) / (maxValue - minValue), 0, 1)
end

function UI:SetSliderValue(control, percent)
    local minValue = control.min or 0
    local maxValue = control.max or 1
    local stepValue = control.step or 1
    local rawValue = minValue + ((maxValue - minValue) * math.clamp(percent, 0, 1))
    local steppedValue = minValue + math.floor(((rawValue - minValue) / stepValue) + 0.5) * stepValue
    steppedValue = math.clamp(steppedValue, minValue, maxValue)

    if control.value ~= steppedValue then
        control.value = steppedValue
        if control.id == "ui_scale" then
            self.window.width = steppedValue
            self:MarkShellDirty()
        elseif control.id == "ui_height" then
            self.window.height = steppedValue
            self:MarkShellDirty()
        elseif control.id == "tween_speed" then
            if steppedValue > 120 and not self.tweenSpeedWarned then
                self.tweenSpeedWarned = true
                self:NotifyPlayer("Tween speeds above 120 may get you banned", "Windy", 4)
            elseif steppedValue <= 120 then
                self.tweenSpeedWarned = false
            end
        end
        self:MarkCurrentTabDirty()
        self:SaveConfig(false)
    end
end

function UI:SetSliderFromMouse(control, mouseX)
    local sliderX = control._sliderX or 0
    local sliderW = control._sliderW or 1
    local percent = (mouseX - sliderX) / sliderW
    self:SetSliderValue(control, percent)
end

function UI:GetFormattedDropdownValue(control)
    if control.type == "dropdown_single" then
        return tostring(control.value or "Select")
    end

    local active = {}
    for _, option in ipairs(control.options or {}) do
        if control.selected and control.selected[option] then
            table.insert(active, option)
        end
    end

    if #active == 0 then
        return "None"
    end

    if #active <= 2 then
        return table.concat(active, ", ")
    end

    return active[1] .. ", " .. active[2] .. " +" .. tostring(#active - 2)
end

function UI:DrawToggle(bucket, control, x, y, w, h)
    local fill = self.animationState.toggleFill[control.id] or (control.value and 1 or 0)
    local boxSize = h
    local radius = math.max(3, math.floor(boxSize / 4))

    if fill <= 0.01 then
        self:CreateSquare(bucket, x, y, boxSize, boxSize, LerpColor(Theme.panelAlt, Theme.accentSoft, fill), 1, true)
        self:CreateSquare(bucket, x, y, boxSize, boxSize, LerpColor(Theme.border, Theme.accent, fill), 1, false, 1)
    else
        local borderColor = LerpColor(Theme.border, Theme.accent, fill)
        local fillColor = LerpColor(Theme.panelAlt, Theme.accent, fill)
        self:CreateRoundedBox(bucket, x, y, boxSize, boxSize, radius, borderColor)
        self:CreateRoundedBox(bucket, x + 1, y + 1, boxSize - 2, boxSize - 2, math.max(2, radius - 1), fillColor)
    end
end

function UI:DrawCheckbox(bucket, control, x, y, w, h)
    local fill = self.animationState.checkboxFill[control.id] or (control.value and 1 or 0)
    self:CreateSquare(bucket, x, y, w, h, LerpColor(Theme.panelAlt, Theme.accentSoft, fill), 1, true)
    self:CreateSquare(bucket, x, y, w, h, LerpColor(Theme.border, Theme.accent, fill), 1, false, 1)
    if fill > 0.01 then
        local inset = 4 + math.floor((1 - fill) * 2)
        self:CreateSquare(bucket, x + inset, y + inset, math.max(2, w - (inset * 2)), math.max(2, h - (inset * 2)), Theme.accent, fill, true)
    end
end

function UI:DrawButton(bucket, control, x, y, w, h)
    local activeFlash = control.flashUntil and tick() < control.flashUntil
    local pressOffset = activeFlash and 1 or 0
    local fillColor = activeFlash and Theme.panelAlt or Theme.panel
    local outlineColor = activeFlash and Theme.accentSoft or Theme.border

    self:CreateSquare(bucket, x, y, w, h, fillColor, 1, true)
    self:CreateSquare(bucket, x, y, w, h, outlineColor, 1, false, 1)
    if activeFlash then
        self:CreateLine(bucket, x + 1, y + h - 2, x + w - 2, y + h - 2, Theme.topHighlight, 0.28, 1)
    else
        self:CreateLine(bucket, x + 1, y + 1, x + w - 2, y + 1, Theme.topHighlight, 0.45, 1)
    end
    self:CreateText(bucket, control.label, x + math.floor(w / 2), y + 12 + pressOffset, Theme.text, 12, true)
end

function UI:DrawSlider(bucket, control, x, y, w, h)
    self:CreateText(bucket, tostring(control.value), x + w + 8, y - 2, Theme.text, 12, false)
    self:CreateSquare(bucket, x, y + 5, w, 8, Theme.panelAlt, 1, true)
    self:CreateSquare(bucket, x, y + 6, w, 6, Theme.borderSoft, 1, false, 1)

    local percent = self.animationState.sliderFill[control.id] or self:GetSliderPercent(control)
    local fillW = math.max(8, math.floor(w * percent))
    self:CreateSquare(bucket, x, y + 5, fillW, 8, Theme.accent, 1, true)
    local handleX = x + math.floor(w * percent)
    self:CreateSquare(bucket, handleX - 3, y + 3, 6, 12, Theme.text, 1, true)
    self:CreateSquare(bucket, handleX - 3, y + 3, 6, 12, Theme.border, 1, false, 1)

    control._sliderX = x
    control._sliderW = w
end

function UI:DrawDropdown(bucket, control, x, y, w, h)
    if self.expandedDropdown == control.id then
        return
    end

    self:CreateSquare(bucket, x, y, w, h, Theme.panel, 1, true)
    self:CreateSquare(bucket, x, y, w, h, Theme.border, 1, false, 1)
    self:CreateLine(bucket, x + 1, y + 1, x + w - 2, y + 1, Theme.topHighlight, 0.45, 1)
    self:CreateLine(bucket, x + w - 34, y + 5, x + w - 34, y + h - 6, Theme.borderSoft, 0.65, 1)
    local displayValue = tostring(self:GetFormattedDropdownValue(control) or "")
    if #displayValue > 22 then
        displayValue = string.sub(displayValue, 1, 19) .. "..."
    end
    self:CreateText(bucket, displayValue, x + 12, y + 7, Theme.text, 12, false)
    self:CreateText(bucket, "v", x + w - 21, y + 7, Theme.text, 12, false)
end

function UI:HandleControlClick(control)
    if control.type == "toggle" or control.type == "checkbox" then
        local nextValue = not control.value
        if (control.id == "car_fly_toggle" or control.id == "car_speed_toggle") and nextValue and not self:CanUsePremiumFeature() then
            self:NotifyPlayer("This is a premium feature only", "Bloxburg", 3)
            self:MarkCurrentTabDirty()
            return
        end
        control.value = nextValue
        if control.id == "farm_toggle" and not nextValue then
            self:ResetFarmState()
        end
        if control.id == "car_fly_toggle" then
            self.carFlyEnabled = nextValue
            if nextValue then
                self:NotifyPlayer("Press F to enable or disable Car Fly", "Bloxburg", 3)
            else
                self.carFlyActive = false
                self.carFlyPosition = nil
                self.carFlyTargetPosition = nil
            end
        end
        if control.id == "ui_animations" then
            self.needsRedraw = true
        end
        self:SaveConfig(control.id == "auto_save_config")
    elseif control.type == "button" then
        control.flashUntil = tick() + 0.35
        if control.id == "discord_button" then
            if type(setclipboard) == "function" then
                pcall(setclipboard, "https://discord.gg/Hu7BQwkJ3y")
            end
            self:NotifyPlayer("Discord link copied to clipboard", "Windy", 3)
        elseif control.id == "panic_reset_button" then
            self:PanicReset()
        elseif control.id == "goto_job_button" then
            self:GotoSelectedJob()
        end
    elseif control.type == "dropdown_single" or control.type == "dropdown_multi" then
        self:ToggleDropdown(control.id)
        return
    end

    self:MarkCurrentTabDirty()
end

function UI:HandleDropdownOption(control, option)
    if control.type == "dropdown_single" then
        control.value = option
        self.expandedDropdown = nil
    else
        control.selected = control.selected or {}
        control.selected[option] = not control.selected[option]
    end

    if control.id == "accent_theme" then
        self:ApplyAccentTheme(control.value)
        self:MarkShellDirty()
    elseif control.id == "job_selection" then
        if self:IsPremiumJob(control.value) and not self:CanUsePremiumFeature() then
            self:NotifyPlayer("This job is a premium feature only", "Bloxburg", 3)
            control.value = "Select Job"
        end
    elseif control.id == "player_tp" then
        self:TweenToPlayer(control.value)
        control.value = "Select Player"
    elseif control.id == "city_tp" then
        local targetPosition = CityTeleportTargets[control.value]
        if targetPosition then
            self:TweenWithVehicleTo(targetPosition)
        end
        control.value = "Select Location"
    end

    self:UpdateScrollLimit()
    self:MarkCurrentTabDirty()
    self:SaveConfig(false)
end

function UI:HandleHotkeys()
    if type(iskeypressed) ~= "function" then
        return
    end

    local now = tick()
    if now - self.lastToggleTick < 0.2 then
        return
    end

    local ok, pressed = pcall(iskeypressed, VK_Z)
    if ok and pressed then
        self.uiVisible = not self.uiVisible
        if self.uiVisible then
            self.contentAlpha = 0.85
            self.contentOffset = 4
        end
        self:MarkShellDirty()
        self:MarkCurrentTabDirty()
        self.lastToggleTick = now
    end
end

function UI:HandleTransientEffects()
    local now = tick()
    local dirty = false

    for _, tabControls in ipairs(self.controls) do
        for _, control in ipairs(tabControls) do
            if control.type == "button" and control.flashUntil and now >= control.flashUntil then
                control.flashUntil = nil
                dirty = true
            end
        end
    end

    if dirty then
        self:MarkCurrentTabDirty()
    end
end

function UI:HandleInput()
    local mousePos = self:GetMouseLocation()
    local mouseDown = self:IsMousePressed()

    if self.draggingWindow and mouseDown then
        local nextX = math.floor(mousePos.X - self.dragOffsetX)
        local nextY = math.floor(mousePos.Y - self.dragOffsetY)
        if self.window.x ~= nextX or self.window.y ~= nextY then
            self.window.x = nextX
            self.window.y = nextY
            self:MarkShellDirty()
            self:MarkCurrentTabDirty()
        end
    elseif self.draggingWindow and not mouseDown then
        self.draggingWindow = false
    end

    if self.draggingSlider and self.activeSlider and mouseDown then
        self:SetSliderFromMouse(self.activeSlider, mousePos.X)
    elseif self.draggingSlider and not mouseDown then
        self.draggingSlider = false
        self.activeSlider = nil
    end

    if self.draggingScroll and mouseDown then
        local scrollbarX, scrollbarY, scrollbarW, scrollbarH, _, thumbH = self:GetScrollbarMetrics()
        local available = math.max(1, scrollbarH - thumbH)
        local newThumbY = math.clamp(mousePos.Y - self.scrollDragOffset, scrollbarY, scrollbarY + available)
        self:SetScrollOffset(((newThumbY - scrollbarY) / available) * self.scroll.max)
    elseif self.draggingScroll and not mouseDown then
        self.draggingScroll = false
    end

    local hovered = nil
    local clickedHitbox = false
    for index = #self.hitboxes, 1, -1 do
        local hitbox = self.hitboxes[index]
        if mousePos.X >= hitbox.x and mousePos.X <= hitbox.x + hitbox.w and mousePos.Y >= hitbox.y and mousePos.Y <= hitbox.y + hitbox.h then
            hovered = hitbox.id
            if mouseDown and not self.lastMousePressed and hitbox.callback then
                clickedHitbox = true
                hitbox.callback()
            end
            break
        end
    end

    if mouseDown and not self.lastMousePressed and not clickedHitbox then
        local canDragWindow = (not self.isBooting and self:IsPointInMainWindow(mousePos))
            or (self.isBooting and self:IsPointInLoadingWindow(mousePos))

        if canDragWindow then
            self.draggingWindow = true
            self.dragOffsetX = mousePos.X - self.window.x
            self.dragOffsetY = mousePos.Y - self.window.y
        end
    end

    if hovered ~= self.hoveredId then
        local previousHovered = self.hoveredId
        local previousIsShell = self:IsShellHitboxId(previousHovered)
        local hoveredIsShell = self:IsShellHitboxId(hovered)
        self.hoveredId = hovered
        if self.isBooting then
            self.needsRedraw = true
        else
            if previousIsShell or hoveredIsShell then
                self:MarkShellDirty()
            end
            if (previousHovered ~= nil and not previousIsShell) or (hovered ~= nil and not hoveredIsShell) then
                self:MarkCurrentTabDirty()
            end
        end
    end

    self.lastMousePressed = mouseDown
end

function UI:RegisterShellHitboxes()
    local wx, wyBase = self:GetRenderWindowPosition()
    local wy = wyBase + self:GetAnimatedWindowYOffset()
    local ww = self.window.width
    local sidebarW = self.window.sidebarWidth
    local tabY = wy + 110

    for tabIndex, _ in ipairs(self.tabs) do
        local buttonY = tabY + ((tabIndex - 1) * 32)
        local buttonX = wx + 14
        local buttonW = sidebarW - 28
        local buttonH = 27

        self:RegisterHitbox("tab_" .. tostring(tabIndex), buttonX, buttonY, buttonW, buttonH, function()
            if self.selectedTab ~= tabIndex then
                self.selectedTab = tabIndex
                self.expandedDropdown = nil
                self.scroll.offset = 0
                self:ResetContentAnimation()
                self:UpdateScrollLimit()
                self:MarkShellDirty()
                self:MarkCurrentTabDirty()
            end
        end, 1)
    end

    self:RegisterHitbox("window_drag", wx, wy, ww, 18, function()
        self.draggingWindow = true
        local mousePos = self:GetMouseLocation()
        self.dragOffsetX = mousePos.X - wx
        self.dragOffsetY = mousePos.Y - wy
    end, 1)
end

function UI:RegisterLoadingHitboxes()
    local loadingScale = 0.4
    local loadW = math.floor(self.window.width * loadingScale)
    local loadH = math.floor(self.window.height * loadingScale)
    local viewport = self:GetViewportSize()
    local loadX = math.floor((viewport.X - loadW) * 0.5)
    local loadY = math.floor((viewport.Y - loadH) * 0.5)

    if self.bootReady then
        local buttonW = 78
        local buttonH = 24
        local barY = loadY + loadH - 22
        local buttonX = loadX + math.floor((loadW - buttonW) * 0.5)
        local buttonY = barY - 34

        self:RegisterHitbox("loading_load_button", buttonX, buttonY, buttonW, buttonH, function()
            self.isBooting = false
            self.bootReady = false
            if self:AreAnimationsEnabled() then
                self.uiAlpha = 0
                self.renderScale = 0.82
                self.contentAlpha = 0
                self.contentOffset = 4
            else
                self.uiAlpha = 1
                self.renderScale = 1
                self.contentAlpha = 1
                self.contentOffset = 0
            end
            self.needsRedraw = true
        end, 3)
    end

    self:RegisterHitbox("loading_drag", loadX, loadY, loadW, 18, function()
        self.draggingWindow = true
        local mousePos = self:GetMouseLocation()
        self.dragOffsetX = mousePos.X - self.window.x
        self.dragOffsetY = mousePos.Y - self.window.y
    end, 1)
end

function UI:RegisterActiveTabHitboxes()
    local x, y, width, height = self:GetContentBounds()
    local cardX = x + 12
    local cardW = width - 28
    local drawY = y + 10 + math.floor((self.contentOffset or 0) + 0.5) - self.scroll.offset
    local bottomY = y + height

    for _, control in ipairs(self:GetActiveControls()) do
        local dropdownProgress = self.animationState.dropdownOpen[control.id] or ((self.expandedDropdown == control.id) and 1 or 0)
        local optionCount = control.options and #control.options or 0
        local extraHeight = math.floor((((optionCount) * 30) + 8) * dropdownProgress)
        local cardH = ((control.type == "slider") and 56 or 44) + extraHeight
        local baseCardHeight = cardH - extraHeight
        local shouldDrawCard = drawY >= y and (drawY + baseCardHeight) <= bottomY

        if shouldDrawCard then
            if control.type == "slider" then
                self:RegisterHitbox(control.id, cardX + 16, drawY + 26, cardW - 62, 24, function()
                    self.draggingSlider = true
                    self.activeSlider = control
                    self:SetSliderFromMouse(control, self:GetMouseLocation().X)
                end, 2)
            elseif control.type ~= "stat" then
                self:RegisterHitbox(control.id, cardX, drawY, cardW, baseCardHeight, function()
                    self:HandleControlClick(control)
                end, 1)
            end

            if dropdownProgress > 0.96 and (control.type == "dropdown_single" or control.type == "dropdown_multi") then
                local listY = drawY + baseCardHeight
                local listH = math.max(0, extraHeight - 2)
                for optionIndex, option in ipairs(control.options or {}) do
                    local rowY = listY + 4 + ((optionIndex - 1) * 30)
                    if rowY + 26 <= listY + listH + 2 then
                        local optionHoverId = control.id .. "_opt_" .. optionIndex
                        self:RegisterHitbox(optionHoverId, cardX + 4, rowY, cardW - 8, 26, function()
                            self:HandleDropdownOption(control, option)
                        end, 2)
                    end
                end
            end
        end

        drawY = drawY + cardH + 10
    end

    local scrollbarX, scrollbarY, scrollbarW, scrollbarH, thumbY, thumbH = self:GetScrollbarMetrics()
    if self.scroll.max > 0 then
        local animatedThumbY = math.floor((self.animatedScrollThumbY or thumbY) + 0.5)
        self:RegisterHitbox("scrollbar_thumb", scrollbarX - 10, animatedThumbY - 2, scrollbarW + 20, thumbH + 4, function()
            self.draggingScroll = true
            self.scrollDragOffset = self:GetMouseLocation().Y - animatedThumbY
        end, 3)
    end
end

function UI:BuildShell()
    local bucket = self.shellBucket
    self:ClearBucket(bucket)
    bucket.deferVisible = true
    self.renderAlpha = self.uiAlpha

    local wx, wyBase = self:GetRenderWindowPosition()
    local wy = wyBase + self:GetAnimatedWindowYOffset()
    local ww = self.window.width
    local wh = self.window.height
    local sidebarW = self.window.sidebarWidth

    self:CreateSquare(bucket, wx, wy, ww, wh, Theme.bg, 1, true)
    self:CreateSquare(bucket, wx, wy, ww, wh, Theme.accent, 1, false, 1)
    self:CreateSquare(bucket, wx + 1, wy + 1, ww - 2, wh - 2, Theme.border, 1, false, 1)
    self:CreateSquare(bucket, wx + 1, wy + 1, sidebarW - 1, wh - 2, Theme.sidebar, 1, true)
    self:CreateLine(bucket, wx + sidebarW, wy + 2, wx + sidebarW, wy + wh - 2, Theme.borderSoft, 1, 1)

    local iconX = wx + 16
    local iconY = wy + 16
    local logoImageData = self:GetLogoImageData()
    local drewLogoImage = false
    if logoImageData then
        local imageObject = self:CreateImage(bucket, logoImageData, iconX - 3, iconY - 3, 41, 41, 1)
        drewLogoImage = imageObject ~= nil
    end

    if not drewLogoImage then
        self:CreateText(bucket, "W", iconX + 17, iconY + 8, Theme.success, 16, true)
    end
    self:CreateText(bucket, "Windy", iconX + 44, iconY + 6, Theme.text, 17, false)
    self:CreateText(bucket, "Bloxburg - " .. self:GetVersionStatusText(), iconX + 44, iconY + 22, Theme.textDim, 12, false)
    self:CreateText(bucket, "TABS", wx + 16, wy + 82, Theme.textDim, 12, false)

    local tabY = wy + 110
    local highlightY = wy + math.floor((self.activeTabHighlightY or 110) + 0.5)
    self:CreateSquare(bucket, wx + 14, highlightY, sidebarW - 28, 27, Theme.accent, 1, true)
    self:CreateSquare(bucket, wx + 14, highlightY + 2, 2, 23, Theme.success, 0.18, true)
    self:CreateLine(bucket, wx + 18, highlightY + 2, wx + sidebarW - 17, highlightY + 2, Theme.success, 0.1, 1)

    for tabIndex, tab in ipairs(self.tabs) do
        local selectedAmount = self.animationState.tabActive[tabIndex] or (self.selectedTab == tabIndex and 1 or 0)
        local hoverAmount = self.animationState.tabHover[tabIndex] or 0
        local buttonY = tabY + ((tabIndex - 1) * 32)
        local buttonX = wx + 14
        local buttonW = sidebarW - 28
        local buttonH = 27

        self:CreateSquare(bucket, buttonX, buttonY, buttonW, buttonH, LerpColor(Theme.shell, Theme.panel, hoverAmount), 1 - (selectedAmount * 0.95), true)
        if hoverAmount > 0.01 and selectedAmount < 0.99 then
            self:CreateSquare(bucket, buttonX + 1, buttonY + 1, buttonW - 2, buttonH - 2, Theme.topHighlight, 0.04 * hoverAmount, true)
        end

        self:CreateText(
            bucket,
            tab.name,
            wx + 24,
            buttonY + 7,
            LerpColor(LerpColor(Theme.textDim, Theme.text, hoverAmount), Theme.success, selectedAmount),
            13,
            false
        )

        self:RegisterHitbox("tab_" .. tostring(tabIndex), buttonX, buttonY, buttonW, buttonH, function()
            if self.selectedTab ~= tabIndex then
                self.selectedTab = tabIndex
                self.expandedDropdown = nil
                self.scroll.offset = 0
                self:ResetContentAnimation()
                self:UpdateScrollLimit()
                self:MarkShellDirty()
                self:MarkCurrentTabDirty()
            end
        end, 1)
    end

    self:CreateText(bucket, "Z = toggle", wx + ww - 72, wy + wh - 16, Theme.textMuted, 12, false)

    self:RegisterHitbox("window_drag", wx, wy, ww, 18, function()
        self.draggingWindow = true
        local mousePos = self:GetMouseLocation()
        self.dragOffsetX = mousePos.X - wx
        self.dragOffsetY = mousePos.Y - wy
    end, 1)

    bucket.built = true
    bucket.deferVisible = false
    self:ShowBucket(bucket)
    self.renderAlpha = 1
end

function UI:BuildLoadingScreen()
    local bucket = self.loadingBucket
    self:ClearBucket(bucket)
    bucket.deferVisible = true
    self.renderAlpha = 1

    local loadingScale = 0.4
    local loadW = math.floor(self.window.width * loadingScale)
    local loadH = math.floor(self.window.height * loadingScale)
    local viewport = self:GetViewportSize()
    local loadX = math.floor((viewport.X - loadW) * 0.5)
    local loadY = math.floor((viewport.Y - loadH) * 0.5)

    self.renderScale = self.bootPanelScale or 0.4

    self:CreateSquare(bucket, loadX, loadY, loadW, loadH, Theme.bg, 1, true)
    self:CreateSquare(bucket, loadX, loadY, loadW, loadH, Theme.accent, 1, false, 1)
    self:CreateSquare(bucket, loadX + 1, loadY + 1, loadW - 2, loadH - 2, Theme.border, 1, false, 1)
    self:CreateSquare(bucket, loadX, loadY, loadW, 2, Theme.accent, 1, true)

    local iconSize = 52
    local iconX = loadX + math.floor((loadW - iconSize) * 0.5)
    local iconY = loadY + 18
    local logoImageData = self:GetLogoImageData()
    local drewLogoImage = false

    if logoImageData then
        local imageObject = self:CreateImage(bucket, logoImageData, iconX, iconY, iconSize, iconSize, 1)
        drewLogoImage = imageObject ~= nil
    end

    if not drewLogoImage then
        self:CreateText(bucket, "W", iconX + math.floor(iconSize * 0.5), iconY + 12, Theme.success, 24, true)
    end

    self:CreateText(
        bucket,
        self.bootReady and "Ready" or "Loading...",
        loadX + math.floor(loadW * 0.5),
        loadY + 92,
        self.bootReady and Theme.text or Theme.textDim,
        13,
        true
    )

    local progress = math.clamp(self.bootVisualProgress or 0, 0, 1)
    local barW = math.max(80, loadW - 40)
    local barX = loadX + math.floor((loadW - barW) * 0.5)
    local barY = loadY + loadH - 22
    self:CreateSquare(bucket, barX, barY, barW, 8, Theme.panelAlt, 1, true)
    self:CreateSquare(bucket, barX, barY, barW, 8, Theme.borderSoft, 1, false, 1)
    self:CreateSquare(bucket, barX, barY, math.max(8, math.floor(barW * progress)), 8, Theme.accent, 1, true)

    if self.bootReady then
        local buttonW = 78
        local buttonH = 24
        local buttonX = loadX + math.floor((loadW - buttonW) * 0.5)
        local buttonY = barY - 34
        local buttonHovered = self.hoveredId == "loading_load_button"

        self:CreateSquare(bucket, buttonX, buttonY, buttonW, buttonH, buttonHovered and Theme.panelAlt or Theme.panel, 1, true)
        self:CreateSquare(bucket, buttonX, buttonY, buttonW, buttonH, Theme.border, 1, false, 1)
        self:CreateLine(bucket, buttonX + 1, buttonY + 1, buttonX + buttonW - 2, buttonY + 1, Theme.topHighlight, 0.45, 1)
        self:CreateText(bucket, "Load", buttonX + math.floor(buttonW * 0.5), buttonY + 12, Theme.text, 12, true)

        self:RegisterHitbox("loading_load_button", buttonX, buttonY, buttonW, buttonH, function()
            self.isBooting = false
            self.bootReady = false
            if self:AreAnimationsEnabled() then
                self.uiAlpha = 0
                self.renderScale = 0.82
                self.contentAlpha = 0
                self.contentOffset = 4
            else
                self.uiAlpha = 1
                self.renderScale = 1
                self.contentAlpha = 1
                self.contentOffset = 0
            end
            self.needsRedraw = true
        end, 3)
    end

    self:RegisterHitbox("loading_drag", loadX, loadY, loadW, 18, function()
        self.draggingWindow = true
        local mousePos = self:GetMouseLocation()
        self.dragOffsetX = mousePos.X - self.window.x
        self.dragOffsetY = mousePos.Y - self.window.y
    end, 1)

    bucket.built = true
    bucket.deferVisible = false
    self:ShowBucket(bucket)
    self.renderAlpha = 1
    self.renderScale = 1
end

function UI:BuildActiveTab()
    local bucket = self.tabBuckets[self.selectedTab]
    self:ClearBucket(bucket)
    bucket.deferVisible = true
    self.renderAlpha = self.uiAlpha * self.contentAlpha

    local x, y, width, height = self:GetContentBounds()
    local cardX = x + 12
    local cardW = width - 28
    local drawY = y + 10 + math.floor((self.contentOffset or 0) + 0.5) - self.scroll.offset
    local bottomY = y + height

    for _, control in ipairs(self:GetActiveControls()) do
        local dropdownProgress = self.animationState.dropdownOpen[control.id] or ((self.expandedDropdown == control.id) and 1 or 0)
        local expanded = dropdownProgress > 0.01
        local optionCount = control.options and #control.options or 0
        local extraHeight = math.floor((((optionCount) * 30) + 8) * dropdownProgress)
        local cardH = ((control.type == "slider") and 56 or 44) + extraHeight
        local baseCardHeight = cardH - extraHeight

        local shouldDrawCard = drawY >= y and (drawY + baseCardHeight) <= bottomY

        if shouldDrawCard then
            if control.type == "stat" then
                self:DrawCardFrame(bucket, cardX, drawY, cardW, baseCardHeight, Theme.border)
                self:CreateText(bucket, control.label, cardX + 16, drawY + 16, Theme.textMuted, 13, false)
                self:CreateRightText(bucket, tostring(control.value or "-"), cardX + cardW - 18, drawY + 16, Theme.text, 13)
            else
                local hovered = self.animationState.controlHover[control.id] or 0
                self:DrawCardFrame(bucket, cardX, drawY, cardW, baseCardHeight, LerpColor(Theme.border, Theme.topHighlight, hovered))
                self:CreateText(bucket, control.label, cardX + 16, drawY + 16, Theme.text, 13, false)

                if control.type == "toggle" then
                    self:DrawToggle(bucket, control, cardX + cardW - 32, drawY + 14, 16, 16)
                elseif control.type == "checkbox" then
                    self:DrawCheckbox(bucket, control, cardX + cardW - 32, drawY + 14, 16, 16)
                elseif control.type == "button" then
                    self:DrawButton(bucket, control, cardX + cardW - 128, drawY + 10, 114, 24)
                elseif control.type == "slider" then
                    self:DrawSlider(bucket, control, cardX + 16, drawY + 32, cardW - 86, 18)
                else
                    self:DrawDropdown(bucket, control, cardX + 176, drawY + 10, cardW - 192, 24)
                end

                if control.type == "slider" then
                    self:RegisterHitbox(control.id, cardX + 16, drawY + 26, cardW - 62, 24, function()
                        self.draggingSlider = true
                        self.activeSlider = control
                        self:SetSliderFromMouse(control, self:GetMouseLocation().X)
                    end, 2)
                else
                    self:RegisterHitbox(control.id, cardX, drawY, cardW, baseCardHeight, function()
                        self:HandleControlClick(control)
                    end, 1)
                end

                if expanded then
                    local listY = drawY + baseCardHeight
                    local listH = math.max(0, extraHeight - 2)
                    self:CreateSquare(bucket, cardX, listY, cardW, listH, Theme.shell, 1, true)
                    self:CreateSquare(bucket, cardX, listY, cardW, listH, Theme.border, dropdownProgress, false, 1)
                    self:CreateLine(bucket, cardX + 1, listY + 1, cardX + cardW - 2, listY + 1, Theme.topHighlight, 0.35 * dropdownProgress, 1)

                    for optionIndex, option in ipairs(control.options) do
                        local rowY = listY + 4 + ((optionIndex - 1) * 30)
                        local selected = control.type == "dropdown_single"
                            and control.value == option
                            or (control.selected and control.selected[option] == true)
                        local premiumOption = control.id == "job_selection" and self:IsPremiumJob(option)
                        local lockedPremiumOption = premiumOption and not self:CanUsePremiumFeature()
                        local optionHoverId = control.id .. "_opt_" .. optionIndex
                        local optionHovered = self.hoveredId == optionHoverId

                        if rowY + 26 <= listY + listH + 2 then
                            local fillColor = selected and Theme.accentSoft or (optionHovered and Theme.panelAlt or Theme.panel)
                            local borderColor = selected and Theme.accent or (optionHovered and Theme.topHighlight or Theme.borderSoft)
                            local textColor = lockedPremiumOption and Theme.textMuted or Theme.text
                            if lockedPremiumOption and not selected then
                                fillColor = Theme.sidebar
                                borderColor = Theme.borderSoft
                            end
                            self:CreateSquare(bucket, cardX + 4, rowY, cardW - 8, 26, fillColor, dropdownProgress, true)
                            self:CreateSquare(bucket, cardX + 4, rowY, cardW - 8, 26, borderColor, dropdownProgress, false, 1)
                            local previousAlpha = self.renderAlpha
                            self.renderAlpha = previousAlpha * dropdownProgress
                            self:CreateText(bucket, option, cardX + 14, rowY + 6, textColor, 12, false)
                            self.renderAlpha = previousAlpha
                            if selected then
                                self:CreateSquare(bucket, cardX + cardW - 24, rowY + 8, 8, 8, Theme.accent, dropdownProgress, true)
                            end

                            if dropdownProgress > 0.96 then
                                self:RegisterHitbox(optionHoverId, cardX + 4, rowY, cardW - 8, 26, function()
                                    self:HandleDropdownOption(control, option)
                                end, 2)
                            end
                        end
                    end
                end
            end
        end

        drawY = drawY + cardH + 10
    end

    local scrollbarX, scrollbarY, scrollbarW, scrollbarH, thumbY, thumbH = self:GetScrollbarMetrics()
    self:CreateSquare(bucket, scrollbarX, scrollbarY, scrollbarW, scrollbarH, Theme.panel, 1, true)
    self:CreateSquare(bucket, scrollbarX, scrollbarY, scrollbarW, scrollbarH, Theme.borderSoft, 1, false, 1)

    if self.scroll.max > 0 and self.scroll.offset < (self.scroll.max - 2) then
        local indicatorY = y + height - 14
        self:CreateText(bucket, "v", x + math.floor((width - 16) * 0.5), indicatorY, Theme.textDim, 12, true)
    end

    if self.scroll.max <= 0 then
        self:CreateSquare(bucket, scrollbarX + 1, scrollbarY + 1, scrollbarW - 2, scrollbarH - 2, Theme.panelAlt, 0.55, true)
    else
        local animatedThumbY = math.floor((self.animatedScrollThumbY or thumbY) + 0.5)
        self:CreateSquare(bucket, scrollbarX + 1, animatedThumbY, scrollbarW - 2, thumbH, Theme.accent, 1, true)
        self:CreateLine(bucket, scrollbarX + 1, animatedThumbY, scrollbarX + scrollbarW - 2, animatedThumbY, Theme.success, 0.18, 1)
        self:RegisterHitbox("scrollbar_thumb", scrollbarX - 10, animatedThumbY - 2, scrollbarW + 20, thumbH + 4, function()
            self.draggingScroll = true
            self.scrollDragOffset = self:GetMouseLocation().Y - animatedThumbY
        end, 3)
    end

    bucket.built = true
    bucket.deferVisible = false
    self:ShowBucket(bucket)
    self.renderAlpha = 1
end

function UI:Render()
    self.hitboxes = {}
    self:RefreshPlayerTeleportOptions()

    if self.isBooting then
        self:HideBucket(self.shellBucket)
        for _, bucket in ipairs(self.tabBuckets) do
            self:HideBucket(bucket)
        end
        self:BuildLoadingScreen()
        self.needsRedraw = false
        return
    end

    self:HideBucket(self.loadingBucket)

    if (not self.uiVisible) and (self.uiAlpha or 0) <= 0.01 then
        self:HideBucket(self.shellBucket)
        self:HideBucket(self.loadingBucket)
        for _, bucket in ipairs(self.tabBuckets) do
            self:HideBucket(bucket)
        end
        self.needsRedraw = false
        return
    end

    self:UpdateScrollLimit()

    for tabIndex, bucket in ipairs(self.tabBuckets) do
        if tabIndex ~= self.selectedTab then
            self:HideBucket(bucket)
        end
    end

    local shellRebuiltThisFrame = false

    if self.shellDirty or not self.shellBucket.built then
        self:BuildShell()
        self.shellDirty = false
        shellRebuiltThisFrame = true
    else
        self:ShowBucket(self.shellBucket)
        self:RegisterShellHitboxes()
    end

    if shellRebuiltThisFrame or self.tabDirty[self.selectedTab] or not self.tabBuckets[self.selectedTab].built then
        self:BuildActiveTab()
        self.tabDirty[self.selectedTab] = false
    else
        self:ShowBucket(self.tabBuckets[self.selectedTab])
        self:RegisterActiveTabHitboxes()
    end

    self.needsRedraw = false
end

UI:ApplyAccentTheme("Green")

pcall(function()
    if UI:LoadConfig() then
        UI.lastConfigSaveTick = tick()
        UI:SetStatValue("last_save_stat", "0s ago")
    end
end)

UI:SanitizePremiumControls()

UI:EnsureFarmWorker()
UI:RefreshHomeStats(true)
UI:Render()

RunService.Heartbeat:Connect(function()
    if not UI.isRunning then
        return
    end

    local now = os.clock()
    local dt = now - (UI.lastCarFlyTick or now)
    UI.lastCarFlyTick = now

    pcall(function()
        UI:UpdateActiveTween()
    end)
    pcall(function()
        UI:SyncTweenGravity(nil)
    end)
    pcall(function()
        UI:UpdateCarFly(dt)
    end)
    pcall(function()
        UI:UpdateCarSpeedModifier()
    end)
end)

RunService.RenderStepped:Connect(function(dt)
    if not UI.isRunning then
        return
    end

    local now = tick()
    local minDelta = UI.minUpdateDelta or (1 / 60)
    local lastTick = UI.lastFrameTick or 0
    if lastTick > 0 and (now - lastTick) < minDelta then
        return
    end
    if lastTick > 0 then
        dt = now - lastTick
    end
    UI.lastFrameTick = now

    UI:UpdateRainbowAccent()

    if UI.isBooting then
        local oldProgress = UI.bootVisualProgress or 0
        local oldTarget = UI.bootVisualTarget or 0
        local oldReady = UI.bootReady == true
        local oldPanelScale = UI.bootPanelScale or 0.4

        pcall(function()
            UI:GetLogoImageData()
        end)

        local elapsed = tick() - UI.bootStartedAt
        local duration = math.max(0.01, UI.bootDuration)
        local timeProgress = math.clamp(elapsed / duration, 0, 1)
        local minProgress = math.min(0.985, timeProgress * 0.9)
        UI.bootPanelScale = LerpNumber(UI.bootPanelScale or 0.4, 1, SmoothFactor(10, dt or (1 / 60)))
        if math.abs((UI.bootPanelScale or 1) - 1) < 0.001 then
            UI.bootPanelScale = 1
        end

        if (UI.bootVisualTarget or 0) <= (UI.bootVisualProgress or 0) + 0.01 then
            local remaining = math.max(minProgress + 0.02, 0.98 - (math.random() * 0.06))
            UI.bootVisualTarget = math.clamp((UI.bootVisualProgress or 0) + (0.03 + (math.random() * 0.14)), 0.08, remaining)
        end

        UI.bootVisualProgress = math.max(
            minProgress,
            LerpNumber(UI.bootVisualProgress or 0, UI.bootVisualTarget or minProgress, SmoothFactor(2.2 + (math.random() * 0.8), dt or (1 / 60)))
        )

        if timeProgress >= 1 then
            UI.bootVisualProgress = 1
        end

        if (tick() - UI.bootStartedAt) >= UI.bootDuration
            and (UI.bootVisualProgress or 0) >= 0.999
            and UI:IsLogoImageReady()
        then
            UI.bootReady = true
        end

        if math.abs((UI.bootVisualProgress or 0) - oldProgress) >= 0.002
            or math.abs((UI.bootVisualTarget or 0) - oldTarget) >= 0.01
            or math.abs((UI.bootPanelScale or 0.4) - oldPanelScale) >= 0.002
            or (UI.bootReady == true) ~= oldReady
        then
            UI.needsRedraw = true
        end
    end

    pcall(function()
        UI:UpdateAnimations(dt or (1 / 60))
    end)

    pcall(function()
        UI:HandleHotkeys()
    end)

    pcall(function()
        UI:RefreshHomeStats(false)
    end)

    if (now - (UI.lastEspUpdateTick or 0)) >= 0.03 then
        UI.lastEspUpdateTick = now
        pcall(function()
            UI:UpdatePizzaBikeEsp()
        end)
        pcall(function()
            UI:UpdatePathDebugDrawings()
        end)
    end

    if UI.isBooting then
        pcall(function()
            UI:HandleInput()
        end)
    elseif UI.uiVisible and (UI.uiAlpha or 0) > 0.9 and (UI.renderScale or 1) > 0.98 then
        pcall(function()
            UI:HandleInput()
        end)

        pcall(function()
            UI:HandleTransientEffects()
        end)
    end

    if UI.needsRedraw then
        pcall(function()
            UI:Render()
        end)
    end
end)
