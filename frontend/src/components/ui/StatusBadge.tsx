import './ui.css';

type StatusColor = 'gray' | 'blue' | 'green' | 'red' | 'yellow' | 'purple' | 'orange' | 'teal';

const statusColors: Record<string, StatusColor> = {
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

interface StatusBadgeProps {
  status: string;
  className?: string;
}

export const StatusBadge = ({ status, className = '' }: StatusBadgeProps): JSX.Element => {
  const color = statusColors[status] || 'gray';
  return <span className={`badge badge-${color} ${className}`}>{status?.replace(/_/g, ' ')}</span>;
};
