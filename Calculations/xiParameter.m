len1 = 66;
len2 = 22;
theta = atan(len1/len2);
a = sqrt(len1^2+len2^2);
lenE = 147;

xi = 0.01:0.01:3;
averageIterations = zeros(1, length(xi));
divergentCalculations = zeros(1, length(xi));

for i = 1:length(xi)
    for repeat = 1:1000
        xf = [100 + 90*rand();100 + 90*rand();-pi/4 + pi/2*rand()]; % These values include most reachable points in space by the arm.
        q = [0;0;0];
        k = calculateKinematics(q, theta, a, lenE);
        iterations = 1;
        % Iterate until error is tolerable. Assumes that 1000 iterations 
        % mean that point is unreachable.
        while(~isErrorTolerable(xf, k) && iterations < 1000)
            invJ = calculateJacobianInverse(q, theta, a, lenE);
            q = q + xi(i)*invJ*(xf-k);
            k = calculateKinematics(q, theta, a, lenE);
            iterations = iterations + 1;
        end
        if iterations ~= 1000
            averageIterations(i) = averageIterations(i) + (iterations - averageIterations(i))/(repeat + 1 - divergentCalculations(i));
        else
            divergentCalculations(i) = divergentCalculations(i) + 1;
        end
    end
end

plot(xi, averageIterations);
xlabel('\xi');
hold on;
plot(xi, divergentCalculations);
xlabel('\xi');
grid on;
grid minor;
legend('iterations', 'divergent calculations')

function k = calculateKinematics(q, theta, a, lenE)
    k = [a*sin(q(1) + q(2) - theta) + a*sin(q(1) - theta) - lenE*cos(q(1) + q(2) + q(3));
            a*cos(q(1) + q(2) - theta) + a*cos(q(1) - theta) + lenE*sin(q(1) + q(2) + q(3));
            q(3)];
end

function r = isErrorTolerable(xf, k)
    error = xf-k;
    r = error(1) < 1 && error(2) < 1 && error(3) < deg2rad(1);
end

function invJ = calculateJacobianInverse(q, theta, a, lenE)
    invJ = [(a*sin(q(1) + q(2) - theta) - lenE*cos(q(1) + q(2) + q(3)))/(sin(q(2))*a^2 - lenE*cos(q(2) + q(3) + theta)*a)                       , (a*cos(q(1) + q(2) - theta) + lenE*sin(q(1) + q(2) + q(3)))/(sin(q(2))*a^2 - lenE*cos(q(2) + q(3) + theta)*a)                       , -(lenE*cos(q(3) + theta))/(a*sin(q(2)) - lenE*cos(q(2) + q(3) + theta))                            ;
                -(a*sin(q(1) + q(2) - theta) + a*sin(q(1) - theta) - lenE*cos(q(1) + q(2) + q(3)))/(sin(q(2))*a^2 - lenE*cos(q(2) + q(3) + theta)*a), -(a*cos(q(1) + q(2) - theta) + a*cos(q(1) - theta) + lenE*sin(q(1) + q(2) + q(3)))/(sin(q(2))*a^2 - lenE*cos(q(2) + q(3) + theta)*a), (lenE*(cos(q(2) + q(3) + theta) + cos(q(3) + theta)))/(a*sin(q(2)) - lenE*cos(q(2) + q(3) + theta));
                0,                                                                                                                                    0,                                                                                                   1];
end