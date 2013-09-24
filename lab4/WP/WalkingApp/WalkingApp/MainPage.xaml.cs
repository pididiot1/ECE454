using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using Microsoft.Phone.Controls;
using Microsoft.Devices.Sensors;
using System.Windows.Threading;
using Microsoft.Xna.Framework;

namespace WalkingApp
{
    public partial class MainPage : PhoneApplicationPage
    {

        static private float fThreshold = 1.25f;

        Compass compass = new Compass();
        Accelerometer accelerometer = new Accelerometer();
        DispatcherTimer timer = new DispatcherTimer();
        bool bStepLocked = false;
        int nSteps = 0;
        

        // Constructor
        public MainPage()
        {
            InitializeComponent();

            compass.CurrentValueChanged += new EventHandler<SensorReadingEventArgs<CompassReading>>(compass_CurrentValueChanged);
            compass.TimeBetweenUpdates = TimeSpan.FromMilliseconds(400);
            compass.Start();

            accelerometer.Start();

            timer.Interval = new TimeSpan(0, 0, 0, 0, 30);
            timer.Tick += timer_OnTimerTick;
            timer.Start();
        }

        public void compass_CurrentValueChanged(object sender, SensorReadingEventArgs<CompassReading> e)
        {
            if (compass.IsDataValid)
            {
                Dispatcher.BeginInvoke(() =>
                {    
                    double dAngle = compass.CurrentValue.TrueHeading;
                    if (dAngle > 330 || dAngle <= 30)
                    {
                        tbDirection.Text = "N";
                    }
                    else if (dAngle > 30 && dAngle <= 60)
                    {
                        tbDirection.Text = "NE";
                    } 
                    else if (dAngle > 60 && dAngle <= 120)
                    {
                        tbDirection.Text = "E";
                    }
                    else if (dAngle > 120 && dAngle <= 150)
                    {
                        tbDirection.Text = "SE";
                    }
                    else if (dAngle > 150 && dAngle <= 210)
                    {
                        tbDirection.Text = "S";
                    }
                    else if (dAngle > 210 && dAngle <= 240)
                    {
                        tbDirection.Text = "SW";
                    }
                    else if (dAngle > 240 && dAngle <= 300)
                    {
                        tbDirection.Text = "W";
                    }
                    else if (dAngle > 300 && dAngle <= 330)
                    {
                        tbDirection.Text = "NW";
                    }
                });                
            }
        }

        public void timer_OnTimerTick(Object sender, EventArgs args)
        {
            if (accelerometer.IsDataValid)
            {
                Vector3 vData = accelerometer.CurrentValue.Acceleration;
                if (Math.Abs(vData.Z) > fThreshold)
                {
                    if (!bStepLocked)
                    {
                        nSteps++;
                        bStepLocked = !bStepLocked;
                        Dispatcher.BeginInvoke(() =>
                        {
                            tbSteps.Text = "Steps: " + nSteps;
                        });   
                    }
                    else
                    {
                        bStepLocked = !bStepLocked;
                    }
                }
            }

        }

    }
}