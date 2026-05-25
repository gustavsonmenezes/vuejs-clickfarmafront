import api from './api';

export const weatherService = {
    getWeather(lat, lon) {
        return api.get('/weather', { params: { lat, lon } });
    }
};
