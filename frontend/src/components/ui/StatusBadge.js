import './ui.css';

const statusColors = {
  PLANNED: 'gray',
  IN_PROGRESS: 'blue',
  COMPLETED: 'green',
  CANCELLED: 'red',
  PAUSED: 'yellow',
  BEGINNER: 'green',
  INTERMEDIATE: 'yellow',
  ADVANCED: 'red',
  STRENGTH: 'purple',
  CARDIO: 'orange',
  FLEXIBILITY: 'teal',
};

export const StatusBadge = ({ status, className = '' }) => {
  const color = statusColors[status] || 'gray';
  return (
    <span className={`badge badge-${color} ${className}`}>
      {status?.replace(/_/g, ' ')}
    </span>
  );
};
